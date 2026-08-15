package dev.estv.pet_crud_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.estv.pet_crud_api.dto.UserDTOs;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController - Testes de Integração")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity adminUser;
    private UserEntity regularUser;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminUser = new UserEntity();
        adminUser.setName("Admin Master");
        adminUser.setEmail("admin@pets.com");
        adminUser.setPassword(passwordEncoder.encode("admin1234"));
        adminUser.setNumber("81900000001");
        adminUser.setRole(UserEntity.Role.ROLE_ADMIN);
        adminUser.setPets(new ArrayList<>());
        userRepository.save(adminUser);
        adminToken = tokenService.generateToken(adminUser);

        regularUser = new UserEntity();
        regularUser.setName("João Silva");
        regularUser.setEmail("joao@email.com");
        regularUser.setPassword(passwordEncoder.encode("senha1234"));
        regularUser.setNumber("81912345678");
        regularUser.setRole(UserEntity.Role.ROLE_USER);
        regularUser.setPets(new ArrayList<>());
        userRepository.save(regularUser);
        userToken = tokenService.generateToken(regularUser);
    }

    @AfterEach
    void tearDown() {
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /users/admin")
    class FindAll {

        @Test
        @DisplayName("Admin deve listar todos os usuários")
        void adminShouldListAllUsers() throws Exception {
            mockMvc.perform(get("/users/admin")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("Usuário comum não deve acessar listagem de usuários")
        void regularUserShouldNotListUsers() throws Exception {
            mockMvc.perform(get("/users/admin")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Não autenticado não deve acessar listagem")
        void unauthenticatedShouldNotListUsers() throws Exception {
            mockMvc.perform(get("/users/admin"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("Deve deletar usuário existente quando autenticado")
        void shouldDeleteExistingUser() throws Exception {
            mockMvc.perform(delete("/users/" + regularUser.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User removed successfully"));
        }

        @Test
        @DisplayName("Deve retornar 404 para usuário inexistente")
        void shouldReturn404ForNonExistentUser() throws Exception {
            UUID randomId = UUID.randomUUID();

            mockMvc.perform(delete("/users/" + randomId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        @DisplayName("Deve retornar 403 ao deletar sem autenticação")
        void shouldReturn403WhenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/users/" + regularUser.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("Deve atualizar usuário com dados válidos")
        void shouldUpdateUserWithValidData() throws Exception {
            UserDTOs.UserUpdate dto = new UserDTOs.UserUpdate(
                    "João Atualizado", "81999999999", "joao@email.com"
            );

            mockMvc.perform(put("/users/" + regularUser.getId())
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User updated succesfuly"));
        }

        @Test
        @DisplayName("Deve retornar 404 para usuário inexistente")
        void shouldReturn404ForNonExistentUser() throws Exception {
            UUID randomId = UUID.randomUUID();
            UserDTOs.UserUpdate dto = new UserDTOs.UserUpdate(
                    "João Atualizado", "81999999999", "joao@email.com"
            );

            mockMvc.perform(put("/users/" + randomId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Deve retornar 403 ao atualizar sem autenticação")
        void shouldReturn403WhenUnauthenticated() throws Exception {
            UserDTOs.UserUpdate dto = new UserDTOs.UserUpdate(
                    "João Atualizado", "81999999999", "joao@email.com"
            );

            mockMvc.perform(put("/users/" + regularUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }
}