package dev.estv.pet_crud_api.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.estv.pet_crud_api.repository.PetRepository;
import dev.estv.pet_crud_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes ponta-a-ponta (E2E) que simulam fluxos completos de uso da API,
 * do registro ao CRUD de pets, sem mocks de camadas intermediárias
 * (exceto integração externa: Cloudinary).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("E2E - Fluxos Completos da API")
class PetCrudE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @MockitoBean
    private dev.estv.pet_crud_api.util.ReturnImageURL returnImageURL;

    @AfterEach
    void tearDown() {
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    // Helpers
    private String registerAndGetToken(String name, String email, String number, String password) throws Exception {
        String body = String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"number\":\"%s\",\"password\":\"%s\"}",
                name, email, number, password
        );

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("data").get("token").asText();
    }

    private String createPetAndGetId(String token) throws Exception {
        when(returnImageURL.imageUrl(any())).thenReturn("http://cloudinary.com/fake.jpg");

        MockMultipartFile image = new MockMultipartFile(
                "image", "pet.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/pets")
                        .file(image)
                        .param("name", "rex caramelo")
                        .param("type", "CÃO")
                        .param("gender", "M")
                        .param("city", "Recife")
                        .param("state", "PE")
                        .param("age", "5")
                        .param("weight", "10")
                        .param("race", "vira-lata")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        // Pet criado — buscamos o id via listagem
        MvcResult listResult = mockMvc.perform(get("/pets")
                        .param("page", "0")
                        .param("items", "1"))
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        return listJson.get("data").get("content").get(0).get("id").asText();
    }

    // Fluxo 1: Registro → Login → Criar Pet → Listar → Deletar
    @Test
    @DisplayName("Fluxo completo: Registro → Login → Criar Pet → Listar → Deletar")
    void fullFlow_RegisterLoginCreateListDelete() throws Exception {

        // 1. Registro
        String registerBody = "{\"name\":\"João Silva\",\"email\":\"joao@email.com\"," +
                "\"number\":\"81912345678\",\"password\":\"senha1234\"}";

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String tokenFromRegister = registerJson.get("data").get("token").asText();
        assertThat(tokenFromRegister).isNotBlank();

        // 2. Login
        String loginBody = "{\"email\":\"joao@email.com\",\"password\":\"senha1234\"}";

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("João Silva"))
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("data").get("token").asText();
        assertThat(token).isNotBlank();

        // 3. Criar pet
        when(returnImageURL.imageUrl(any())).thenReturn("http://cloudinary.com/pet.jpg");
        MockMultipartFile image = new MockMultipartFile(
                "image", "pet.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-content".getBytes()
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
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Pet created successfully"));

        // 4. Listar pets
        MvcResult listResult = mockMvc.perform(get("/pets")
                        .param("page", "0")
                        .param("items", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("rex caramelo"))
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        String petId = listJson.get("data").get("content").get(0).get("id").asText();
        assertThat(petId).isNotBlank();

        // 5. Deletar pet
        mockMvc.perform(delete("/pets/" + petId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pet removed successfully"));

        // 6. Confirmar que lista ficou vazia
        mockMvc.perform(get("/pets")
                        .param("page", "0")
                        .param("items", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // Fluxo 2: Criar Pet → Buscar por filtro → Verificar resultado
    @Test
    @DisplayName("Fluxo de busca: Criar pets → Filtrar → Verificar resultados")
    void fullFlow_CreateAndSearch() throws Exception {
        String token = registerAndGetToken(
                "Maria Costa", "maria@email.com", "81999999999", "senha1234"
        );
        when(returnImageURL.imageUrl(any())).thenReturn("http://cloudinary.com/fake.jpg");
        MockMultipartFile image = new MockMultipartFile(
                "image", "p.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes()
        );

        // Criar 2 cães e 1 gato
        mockMvc.perform(multipart("/pets").file(image)
                        .param("name", "rex caramelo").param("type", "CÃO").param("gender", "M")
                        .param("city", "Recife").param("state", "PE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/pets").file(image)
                        .param("name", "bidu manchado").param("type", "CÃO").param("gender", "M")
                        .param("city", "Olinda").param("state", "PE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/pets").file(image)
                        .param("name", "luna fofinha").param("type", "GATO").param("gender", "F")
                        .param("city", "Recife").param("state", "PE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        // Buscar apenas cães
        mockMvc.perform(post("/pets/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CÃO\"}")
                        .param("page", "0")
                        .param("items", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        // Buscar por cidade Recife
        mockMvc.perform(post("/pets/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Recife\"}")
                        .param("page", "0")
                        .param("items", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        // Buscar cão em Recife
        mockMvc.perform(post("/pets/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CÃO\",\"city\":\"Recife\"}")
                        .param("page", "0")
                        .param("items", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("rex caramelo"));
    }

    // Fluxo 3: Registro duplicado não é permitido
    @Test
    @DisplayName("Fluxo de segurança: Registro duplicado retorna 409")
    void fullFlow_DuplicateRegistration() throws Exception {
        String body = "{\"name\":\"João Silva\",\"email\":\"joao@email.com\"," +
                "\"number\":\"81912345678\",\"password\":\"senha1234\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    // Fluxo 4: Token inválido é rejeitado
    @Test
    @DisplayName("Fluxo de segurança: Token inválido é rejeitado em endpoints protegidos")
    void fullFlow_InvalidTokenIsRejected() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "pet.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes()
        );

        mockMvc.perform(multipart("/pets")
                        .file(image)
                        .param("name", "rex caramelo")
                        .param("type", "CÃO")
                        .param("gender", "M")
                        .param("city", "Recife")
                        .param("state", "PE")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isForbidden());
    }

    // Fluxo 5: Usuário pode se auto-deletar
    @Test
    @DisplayName("Fluxo de conta: Usuário pode deletar sua própria conta")
    void fullFlow_UserSelfDeletion() throws Exception {
        String registerBody = "{\"name\":\"Ana Pereira\",\"email\":\"ana@email.com\"," +
                "\"number\":\"81900000000\",\"password\":\"senha1234\"}";

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String token = json.get("data").get("token").asText();

        // Buscar o id do usuário — via admin não temos acesso com userToken,
        // então pegamos da base diretamente para o teste E2E
        var user = userRepository.findByUsermail("ana@email.com").orElseThrow();

        mockMvc.perform(delete("/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User removed successfully"));

        assertThat(userRepository.findByUsermail("ana@email.com")).isEmpty();
    }
}