package dev.estv.pet_crud_api.service;

import dev.estv.pet_crud_api.dto.PetDTOs;
import dev.estv.pet_crud_api.entity.PetModel;
import dev.estv.pet_crud_api.entity.UserModel;
import dev.estv.pet_crud_api.repository.PetRepository;
import dev.estv.pet_crud_api.repository.UserRepository;
import dev.estv.pet_crud_api.util.PetMapper;
import dev.estv.pet_crud_api.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetService - Testes Unitários")
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetMapper petMapper;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private PetService petService;

    private UserModel defaultUser;
    private PetModel defaultPet;

    @BeforeEach
    void setUp() {
        defaultUser = new UserModel();
        defaultUser.setId(UUID.randomUUID());
        defaultUser.setEmail("joao@email.com");
        defaultUser.setName("João Silva");
        defaultUser.setRole(UserModel.Role.ROLE_USER);
        defaultUser.setPets(new ArrayList<>());

        defaultPet = PetModel.builder()
                .id(UUID.randomUUID())
                .name("rex caramelo")
                .type(PetModel.Type.CAO)
                .gender(PetModel.Gender.M)
                .city("recife")
                .state("PE")
                .age("5")
                .weight("10")
                .race("vira-lata")
                .owner(defaultUser)
                .createdAt(LocalDateTime.now())
                .imageUrl("http://cloudinary.com/img.jpg")
                .build();
    }

    private void authenticateAs(String email) {
        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("listPets()")
    class ListPets {

        @Test
        @DisplayName("Deve retornar página de pets mapeados para DTO")
        void shouldReturnPageOfPetDTOs() {
            PetDTOs.PetResponse dto = new PetDTOs.PetResponse();
            dto.setName("rex caramelo");

            Page<PetModel> petPage = new PageImpl<>(List.of(defaultPet));
            when(petRepository.findAll(any(Pageable.class))).thenReturn(petPage);
            when(petMapper.toDTO(defaultPet)).thenReturn(dto);

            Page<PetDTOs.PetResponse> result = petService.listPets(0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("rex caramelo");
            verify(petRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há pets")
        void shouldReturnEmptyPage() {
            when(petRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            Page<PetDTOs.PetResponse> result = petService.listPets(0, 10);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Deve salvar pet com sucesso")
        void shouldSavePetSuccessfully() {
            authenticateAs("joao@email.com");

            PetDTOs.PetRecord dto = new PetDTOs.PetRecord(
                    "Rex Caramelo", "CÃO", "M", "Recife", "PE", "5", "10", "vira-lata"
            );

            when(userRepository.findByUsermail("joao@email.com"))
                    .thenReturn(Optional.of(defaultUser));
            when(petMapper.toEntity(dto, defaultUser)).thenReturn(defaultPet);
            when(petRepository.save(defaultPet)).thenReturn(defaultPet);

            PetModel saved = petService.save(dto, "http://cloudinary.com/img.jpg");

            assertThat(saved).isNotNull();
            assertThat(saved.getImageUrl()).isEqualTo("http://cloudinary.com/img.jpg");
            verify(petRepository).save(defaultPet);
            verify(validationUtil).validatePet(defaultPet);

            SecurityContextHolder.clearContext();
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deve retornar true e deletar quando pet existe")
        void shouldReturnTrueAndDeleteWhenPetExists() {
            UUID id = defaultPet.getId();
            when(petRepository.existsById(id)).thenReturn(true);

            boolean result = petService.delete(id);

            assertThat(result).isTrue();
            verify(petRepository).deleteById(id);
        }

        @Test
        @DisplayName("Deve retornar false quando pet não existe")
        void shouldReturnFalseWhenPetNotFound() {
            UUID id = UUID.randomUUID();
            when(petRepository.existsById(id)).thenReturn(false);

            boolean result = petService.delete(id);

            assertThat(result).isFalse();
            verify(petRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Deve retornar pet quando encontrado")
        void shouldReturnPetWhenFound() {
            UUID id = defaultPet.getId();
            when(petRepository.findById(id)).thenReturn(Optional.of(defaultPet));

            PetModel found = petService.findById(id);

            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Deve retornar null quando pet não encontrado")
        void shouldReturnNullWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(petRepository.findById(id)).thenReturn(Optional.empty());

            PetModel found = petService.findById(id);

            assertThat(found).isNull();
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Deve atualizar pet e manter imagem antiga quando nova não é fornecida")
        void shouldKeepOldImageWhenNoNewImageProvided() {
            UUID id = defaultPet.getId();
            PetDTOs.PetRecord dto = new PetDTOs.PetRecord(
                    "Rex Novo Nome", "CÃO", "M", "Olinda", "PE", "6", "12", "golden"
            );

            when(petRepository.findById(id)).thenReturn(Optional.of(defaultPet));
            when(petRepository.save(any(PetModel.class))).thenReturn(defaultPet);

            PetModel updated = petService.update(id, dto, null);

            assertThat(updated.getImageUrl()).isEqualTo("http://cloudinary.com/img.jpg");
            verify(petRepository).save(any(PetModel.class));
        }

        @Test
        @DisplayName("Deve atualizar pet com nova imagem quando fornecida")
        void shouldUpdateImageWhenNewImageProvided() {
            UUID id = defaultPet.getId();
            PetDTOs.PetRecord dto = new PetDTOs.PetRecord(
                    "Rex Novo Nome", "CÃO", "M", "Olinda", "PE", "6", "12", "golden"
            );

            when(petRepository.findById(id)).thenReturn(Optional.of(defaultPet));
            when(petRepository.save(any(PetModel.class))).thenAnswer(inv -> inv.getArgument(0));

            PetModel updated = petService.update(id, dto, "http://cloudinary.com/nova.jpg");

            assertThat(updated.getImageUrl()).isEqualTo("http://cloudinary.com/nova.jpg");
        }
    }
}