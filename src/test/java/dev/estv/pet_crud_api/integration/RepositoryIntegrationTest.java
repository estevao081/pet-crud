package dev.estv.pet_crud_api.integration;

import dev.estv.pet_crud_api.dto.PetDTOs;
import dev.estv.pet_crud_api.entity.PetEntity;
import dev.estv.pet_crud_api.entity.UserEntity;
import dev.estv.pet_crud_api.repository.PetRepository;
import dev.estv.pet_crud_api.repository.UserRepository;
import dev.estv.pet_crud_api.specification.PetSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repositories - Testes de Integração")
class RepositoryIntegrationTest {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setName("João Silva");
        testUser.setEmail("joao@email.com");
        testUser.setPassword("encoded_password");
        testUser.setNumber("81912345678");
        testUser.setRole(UserEntity.Role.ROLE_USER);
        testUser.setPets(new ArrayList<>());
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    private PetEntity buildPet(String name, PetEntity.Type type, String city) {
        return PetEntity.builder()
                .name(name)
                .type(type)
                .gender(PetEntity.Gender.M)
                .city(city)
                .state("PE")
                .age("5")
                .weight("10")
                .race("vira-lata")
                .owner(testUser)
                .createdAt(LocalDateTime.now())
                .imageUrl("http://cloudinary.com/img.jpg")
                .build();
    }

    @Nested
    @DisplayName("UserRepository")
    class UserRepositoryTests {

        @Test
        @DisplayName("findByUsermail() deve retornar usuário pelo email")
        void shouldFindUserByEmail() {
            Optional<UserEntity> found = userRepository.findByUsermail("joao@email.com");

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("findByUsermail() deve retornar Optional vazio para email inexistente")
        void shouldReturnEmptyForNonExistentEmail() {
            Optional<UserEntity> found = userRepository.findByUsermail("naoexiste@email.com");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Deve persistir e recuperar usuário com todos os campos")
        void shouldPersistAndRetrieveUser() {
            Optional<UserEntity> found = userRepository.findById(testUser.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("joao@email.com");
            assertThat(found.get().getRole()).isEqualTo(UserEntity.Role.ROLE_USER);
        }
    }

    @Nested
    @DisplayName("PetRepository")
    class PetRepositoryTests {

        @Test
        @DisplayName("Deve salvar e recuperar pet pelo id")
        void shouldSaveAndFindPetById() {
            PetEntity pet = buildPet("rex caramelo", PetEntity.Type.CAO, "recife");
            PetEntity saved = petRepository.save(pet);

            Optional<PetEntity> found = petRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("rex caramelo");
            assertThat(found.get().getOwner().getEmail()).isEqualTo("joao@email.com");
        }

        @Test
        @DisplayName("Deve listar todos os pets com paginação")
        void shouldListAllPetsWithPagination() {
            petRepository.save(buildPet("rex caramelo", PetEntity.Type.CAO, "recife"));
            petRepository.save(buildPet("luna fofinha", PetEntity.Type.GATO, "olinda"));
            petRepository.save(buildPet("bidu manchado", PetEntity.Type.CAO, "caruaru"));

            PageRequest pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());
            Page<PetEntity> page = petRepository.findAll(pageable);

            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve deletar pet pelo id")
        void shouldDeletePetById() {
            PetEntity pet = petRepository.save(buildPet("rex caramelo", PetEntity.Type.CAO, "recife"));

            petRepository.deleteById(pet.getId());

            assertThat(petRepository.existsById(pet.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("PetSpecification - Filtros dinâmicos")
    class PetSpecificationTests {

        @BeforeEach
        void createPets() {
            petRepository.save(buildPet("rex caramelo", PetEntity.Type.CAO, "recife"));
            petRepository.save(buildPet("luna fofinha", PetEntity.Type.GATO, "olinda"));
            petRepository.save(buildPet("bidu manchado", PetEntity.Type.CAO, "caruaru"));
        }

        @Test
        @DisplayName("Deve filtrar por nome parcial (case-insensitive)")
        void shouldFilterByPartialName() {
            PetDTOs.PetResponse filter = new PetDTOs.PetResponse();
            filter.setName("rex");

            List<PetEntity> result = petRepository.findAll(PetSpecification.filter(filter));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("rex caramelo");
        }

        @Test
        @DisplayName("Deve filtrar por tipo")
        void shouldFilterByType() {
            PetDTOs.PetResponse filter = new PetDTOs.PetResponse();
            filter.setType("CÃO");

            List<PetEntity> result = petRepository.findAll(PetSpecification.filter(filter));

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(p -> p.getType() == PetEntity.Type.CAO);
        }

        @Test
        @DisplayName("Deve filtrar por cidade parcial")
        void shouldFilterByCity() {
            PetDTOs.PetResponse filter = new PetDTOs.PetResponse();
            filter.setCity("olinda");

            List<PetEntity> result = petRepository.findAll(PetSpecification.filter(filter));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("luna fofinha");
        }

        @Test
        @DisplayName("Deve retornar todos quando filtro está vazio")
        void shouldReturnAllWhenFilterIsEmpty() {
            PetDTOs.PetResponse filter = new PetDTOs.PetResponse();

            List<PetEntity> result = petRepository.findAll(PetSpecification.filter(filter));

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Deve combinar múltiplos filtros")
        void shouldCombineMultipleFilters() {
            PetDTOs.PetResponse filter = new PetDTOs.PetResponse();
            filter.setType("CÃO");
            filter.setCity("recife");

            List<PetEntity> result = petRepository.findAll(PetSpecification.filter(filter));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("rex caramelo");
        }
    }
}