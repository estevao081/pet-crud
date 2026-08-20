package dev.estv.pet_crud_api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.estv.pet_crud_api.dto.AuthDTOs;
import dev.estv.pet_crud_api.dto.UserDTOs;
import dev.estv.pet_crud_api.entity.UserModel;
import dev.estv.pet_crud_api.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController - Testes de Integração")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /auth/register")
    class Register {

        @Test
        @DisplayName("Deve registrar novo usuário e retornar 201 com token")
        void shouldRegisterAndReturn201() throws Exception {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord(
                    "João Silva", "81912345678", "joao@email.com", "senha1234"
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value("João Silva"))
                    .andExpect(jsonPath("$.message").value("User created successfully"));
        }

        @Test
        @DisplayName("Deve retornar 409 quando email já está em uso")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord(
                    "João Silva", "81912345678", "joao@email.com", "senha1234"
            );

            // Primeiro registro
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());

            // Segundo registro com mesmo email
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("User already exists"));
        }

        @Test
        @DisplayName("Deve retornar 400 quando campos obrigatórios estão vazios")
        void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord("", "", "", "");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Deve retornar 400 para nome sem sobrenome")
        void shouldReturn400ForNameWithoutLastName() throws Exception {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord(
                    "Joao", "81912345678", "joao@email.com", "senha1234"
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @BeforeEach
        void createUser() {
            UserModel user = new UserModel();
            user.setName("João Silva");
            user.setEmail("joao@email.com");
            user.setPassword(passwordEncoder.encode("senha1234"));
            user.setNumber("81912345678");
            user.setRole(UserModel.Role.ROLE_USER);
            userRepository.save(user);
        }

        @Test
        @DisplayName("Deve fazer login com sucesso e retornar token")
        void shouldLoginSuccessfullyAndReturnToken() throws Exception {
            AuthDTOs.LoginRequest dto = new AuthDTOs.LoginRequest("joao@email.com", "senha1234");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value("João Silva"))
                    .andExpect(jsonPath("$.message").value("Login successful"));
        }

        @Test
        @DisplayName("Deve retornar 401 para senha incorreta")
        void shouldReturn401ForWrongPassword() throws Exception {
            AuthDTOs.LoginRequest dto = new AuthDTOs.LoginRequest("joao@email.com", "senhaErrada");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando email não existe")
        void shouldThrowWhenEmailNotFound() throws Exception {
            AuthDTOs.LoginRequest dto = new AuthDTOs.LoginRequest("naoexiste@email.com", "senha1234");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().is5xxServerError());
        }
    }
}