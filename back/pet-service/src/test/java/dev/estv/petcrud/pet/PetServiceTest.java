package dev.estv.petcrud.pet;

import dev.estv.petcrud.pet.client.UserClient;
import dev.estv.petcrud.pet.dto.PetDTOs;
import dev.estv.petcrud.pet.dto.UserSummary;
import dev.estv.petcrud.pet.entity.PetEntity;
import dev.estv.petcrud.pet.exception.InvalidNameException;
import dev.estv.petcrud.pet.exception.PetNotFoundException;
import dev.estv.petcrud.pet.repository.PetRepository;
import dev.estv.petcrud.pet.service.PetService;
import dev.estv.petcrud.pet.util.PetMapper;
import dev.estv.petcrud.pet.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private UserClient userClient;

    private PetService petService;
    private final UUID ownerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        petService = new PetService(petRepository, new PetMapper(), new ValidationUtil(), userClient);
    }

    private PetEntity pet(UUID owner) {
        return PetEntity.builder().id(UUID.randomUUID()).name("rex caramelo").type(PetEntity.Type.CAO)
                .gender(PetEntity.Gender.M).city("recife").state("PE").age("5").weight("10")
                .race("vira-lata").ownerId(owner).build();
    }

    private PetDTOs.PetRecord record(String name) {
        return new PetDTOs.PetRecord(name, "CÃO", "M", "Recife", "PE", "5", "10", "vira-lata");
    }

    @Test
    void listPetsFillsOwnerFromUserService() {
        Page<PetEntity> page = new PageImpl<>(List.of(pet(ownerId)));
        when(petRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userClient.findSummaries(any())).thenReturn(
                Map.of(ownerId, new UserSummary(ownerId, "João Silva", "81912345678")));

        var owner = petService.listPets(0, 9).getContent().get(0).getOwner();

        assertThat(owner.getName()).isEqualTo("João Silva");
        assertThat(owner.getNumber()).isEqualTo(81912345678L);
    }

    @Test
    void listPetsStillWorksWhenUserServiceIsDown() {
        Page<PetEntity> page = new PageImpl<>(List.of(pet(ownerId)));
        when(petRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userClient.findSummaries(any())).thenReturn(Map.of()); // é o que o UserClient devolve em caso de falha

        var pets = petService.listPets(0, 9).getContent();

        assertThat(pets).hasSize(1);
        assertThat(pets.get(0).getOwner()).isNull();
    }

    @Test
    void petWithoutOwnerDoesNotBreakListing() {
        Page<PetEntity> page = new PageImpl<>(List.of(pet(null)));
        when(petRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userClient.findSummaries(any())).thenReturn(Map.of());

        assertThat(petService.listPets(0, 9).getContent().get(0).getOwner()).isNull();
    }

    @Test
    void saveStoresTheOwnerIdFromTheToken() {
        when(petRepository.save(any(PetEntity.class))).thenAnswer(i -> i.getArgument(0));

        petService.save(record("Rex Caramelo"), "http://localhost:8080/images/1", ownerId);

        ArgumentCaptor<PetEntity> captor = ArgumentCaptor.forClass(PetEntity.class);
        verify(petRepository).save(captor.capture());
        assertThat(captor.getValue().getOwnerId()).isEqualTo(ownerId);
        assertThat(captor.getValue().getImageUrl()).isEqualTo("http://localhost:8080/images/1");
    }

    @Test
    void saveValidatesPetRules() {
        assertThatThrownBy(() -> petService.save(record("Rex"), null, ownerId))
                .isInstanceOf(InvalidNameException.class);
    }

    @Test
    void updateOfUnknownPetThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(petRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.update(id, record("Rex Caramelo"), null))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void deleteByOwnerDelegatesToRepository() {
        petService.deleteByOwner(ownerId);

        verify(petRepository).deleteByOwnerId(ownerId);
    }
}
