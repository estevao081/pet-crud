package dev.estv.pet_crud_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.estv.pet_crud_api.entity.PetEntity;
import dev.estv.pet_crud_api.entity.UserEntity;
import dev.estv.pet_crud_api.repository.PetRepository;
import dev.estv.pet_crud_api.repository.UserRepository;
import dev.estv.pet_crud_api.security.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PetController - Testes de Integração")
class PetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Mockamos a integração com Cloudinary para não depender de rede externa
    @MockitoBean
    private dev.estv.pet_crud_api.util.ReturnImageURL returnImageURL;

    private UserEntity testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setName("João Silva");
        testUser.setEmail("joao@email.com");
        testUser.setPassword(passwordEncoder.encode("senha1234"));
        testUser.setNumber("81912345678");
        testUser.setRole(UserEntity.Role.ROLE_USER);
        testUser.setPets(new ArrayList<>());
        userRepository.save(testUser);

        authToken = tokenService.generateToken(testUser);

        when(returnImageURL.imageUrl(any())).thenReturn("http://cloudinary.com/fake.jpg");
    }

    @AfterEach
    void tearDown() {
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    private PetEntity createPetInDb(String name) {
        PetEntity pet = PetEntity.builder()
                .name(name)
                .type(PetEntity.Type.CAO)
                .gender(PetEntity.Gender.M)
                .city("recife")
                .state("PE")
                .age("5")
                .weight("10")
                .race("vira-lata")
                .owner(testUser)
                .createdAt(LocalDateTime.now())
                .imageUrl("http://cloudinary.com/img.jpg")
                .build();
        return petRepository.save(pet);
    }

    @Nested
    @DisplayName("GET /pets")
    class GetPets {

        @Test
        @DisplayName("Deve listar pets paginados sem autenticação")
        void shouldListPetsWithoutAuth() throws Exception {
            createPetInDb("rex caramelo");
            createPetInDb("luna fofinha");

            mockMvc.perform(get("/pets")
                            .param("page", "0")
                            .param("items", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há pets")
        void shouldReturnEmptyPage() throws Exception {
            mockMvc.perform(get("/pets")
                            .param("page", "0")
                            .param("items", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(0)));
        }

        @Test
        @DisplayName("Deve respeitar paginação")
        void shouldRespectPagination() throws Exception {
            createPetInDb("rex caramelo");
            createPetInDb("luna fofinha");
            createPetInDb("bidu manchado");

            mockMvc.perform(get("/pets")
                            .param("page", "0")
                            .param("items", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(2)))
                    .andExpect(jsonPath("$.data.totalElements").value(3));
        }
    }

    @Nested
    @DisplayName("POST /pets")
    class SavePet {

        @Test
        @DisplayName("Deve criar pet com sucesso quando autenticado")
        void shouldCreatePetWhenAuthenticated() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image", "pet.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-content".getBytes()
            );

            mockMvc.perform(multipart("/pets")
                            .file(image)
                            .param("name", "rex caramelo")
                            .param("type", "CÃO")
                            .param("gender", "M")
                            .param("city", "Recife")
                            .param("state", "PE")
                            .param("age", "5")
                            .param("weight", "10")
                            .param("race", "vira-lata")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pet created successfully"));
        }

        @Test
        @DisplayName("Deve retornar 403 quando não autenticado")
        void shouldReturn403WhenNotAuthenticated() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image", "pet.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes()
            );

            mockMvc.perform(multipart("/pets")
                            .file(image)
                            .param("name", "rex caramelo")
                            .param("type", "CÃO")
                            .param("gender", "M")
                            .param("city", "Recife")
                            .param("state", "PE"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 400 para campos obrigatórios ausentes")
        void shouldReturn400ForMissingRequiredFields() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image", "pet.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes()
            );

            mockMvc.perform(multipart("/pets")
                            .file(image)
                            .param("name", "")
                            .param("type", "CÃO")
                            .param("gender", "M")
                            .param("city", "Recife")
                            .param("state", "PE")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /pets/{id}")
    class DeletePet {

        @Test
        @DisplayName("Deve deletar pet existente e retornar 200")
        void shouldDeleteExistingPet() throws Exception {
            PetEntity pet = createPetInDb("rex caramelo");

            mockMvc.perform(delete("/pets/" + pet.getId())
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pet removed successfully"));
        }

        @Test
        @DisplayName("Deve retornar 404 para pet inexistente")
        void shouldReturn404ForNonExistentPet() throws Exception {
            UUID randomId = UUID.randomUUID();

            mockMvc.perform(delete("/pets/" + randomId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Pet not found"));
        }

        @Test
        @DisplayName("Deve retornar 403 ao deletar sem autenticação")
        void shouldReturn403WhenDeleteWithoutAuth() throws Exception {
            PetEntity pet = createPetInDb("rex caramelo");

            mockMvc.perform(delete("/pets/" + pet.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /pets/search")
    class SearchPets {

        @Test
        @DisplayName("Deve buscar pets por nome sem autenticação")
        void shouldSearchByNameWithoutAuth() throws Exception {
            createPetInDb("rex caramelo");
            createPetInDb("luna fofinha");

            String filterJson = "{\"name\": \"rex\"}";

            mockMvc.perform(post("/pets/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(filterJson)
                            .param("page", "0")
                            .param("items", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].name").value("rex caramelo"));
        }

        @Test
        @DisplayName("Deve retornar todos os pets quando filtro está vazio")
        void shouldReturnAllWhenFilterIsEmpty() throws Exception {
            createPetInDb("rex caramelo");
            createPetInDb("luna fofinha");

            mockMvc.perform(post("/pets/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .param("page", "0")
                            .param("items", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("PUT /pets/{id}")
    class UpdatePet {

        @Test
        @DisplayName("Deve retornar 404 ao tentar atualizar pet inexistente")
        void shouldReturn404ForNonExistentPet() throws Exception {
            UUID randomId = UUID.randomUUID();
            MockMultipartFile image = new MockMultipartFile(
                    "image", "pet.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes()
            );

            mockMvc.perform(multipart("/pets/" + randomId)
                            .file(image)
                            .param("name", "rex novo")
                            .param("type", "CÃO")
                            .param("gender", "M")
                            .param("city", "Recife")
                            .param("state", "PE")
                            .with(req -> { req.setMethod("PUT"); return req; })
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }
    }
}