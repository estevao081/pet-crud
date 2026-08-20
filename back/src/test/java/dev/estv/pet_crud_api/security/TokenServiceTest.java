package dev.estv.pet_crud_api.security;

import dev.estv.pet_crud_api.entity.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenService - Testes Unitários")
class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key-for-unit-tests-only");
    }

    private UserModel buildUser() {
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        user.setEmail("joao@email.com");
        user.setName("João Silva");
        user.setRole(UserModel.Role.ROLE_USER);
        return user;
    }

    @Test
    @DisplayName("Deve gerar um token não nulo e não vazio")
    void shouldGenerateNonNullToken() {
        UserModel user = buildUser();
        String token = tokenService.generateToken(user);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Deve validar token e retornar o email do subject")
    void shouldValidateTokenAndReturnEmail() {
        UserModel user = buildUser();
        String token = tokenService.generateToken(user);

        String subject = tokenService.validateToken(token);

        assertThat(subject).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("Deve retornar null para token inválido")
    void shouldReturnNullForInvalidToken() {
        String subject = tokenService.validateToken("token.invalido.aqui");
        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Deve retornar null para token vazio")
    void shouldReturnNullForEmptyToken() {
        String subject = tokenService.validateToken("");
        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Deve extrair a role corretamente do token")
    void shouldExtractRoleFromToken() {
        UserModel user = buildUser();
        user.setRole(UserModel.Role.ROLE_ADMIN);
        String token = tokenService.generateToken(user);

        String role = tokenService.getRole(token);

        assertThat(role).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve retornar null ao tentar extrair role de token inválido")
    void shouldReturnNullRoleForInvalidToken() {
        String role = tokenService.getRole("token.invalido");
        assertThat(role).isNull();
    }

    @Test
    @DisplayName("Tokens gerados para usuários diferentes devem ser distintos")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        UserModel user1 = buildUser();
        UserModel user2 = buildUser();
        user2.setEmail("maria@email.com");

        String token1 = tokenService.generateToken(user1);
        String token2 = tokenService.generateToken(user2);

        assertThat(token1).isNotEqualTo(token2);
    }
}