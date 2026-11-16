package dev.estv.petcrud.auth;

import dev.estv.petcrud.auth.client.UserClient;
import dev.estv.petcrud.auth.dto.AuthDTOs;
import dev.estv.petcrud.auth.dto.UserClientDTOs.CreateUserRequest;
import dev.estv.petcrud.auth.dto.UserClientDTOs.CreatedUser;
import dev.estv.petcrud.auth.dto.UserClientDTOs.UserCredentials;
import dev.estv.petcrud.auth.exception.InvalidCredentialsException;
import dev.estv.petcrud.auth.exception.InvalidPasswordException;
import dev.estv.petcrud.auth.service.AuthService;
import dev.estv.petcrud.auth.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserClient userClient;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final TokenService tokenService = new TokenService("test-secret-key-for-unit-tests-only", 2);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userClient, encoder, tokenService);
    }

    @Test
    void loginReturnsTokenWhenPasswordMatches() {
        UUID id = UUID.randomUUID();
        when(userClient.findByEmail("joao@email.com")).thenReturn(Optional.of(
                new UserCredentials(id, "João Silva", "joao@email.com", encoder.encode("senha1234"), "ROLE_USER")));

        var response = authService.login(new AuthDTOs.LoginRequest("joao@email.com", "senha1234"));

        assertThat(response.name()).isEqualTo("João Silva");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(userClient.findByEmail("joao@email.com")).thenReturn(Optional.of(
                new UserCredentials(UUID.randomUUID(), "João Silva", "joao@email.com", encoder.encode("senha1234"), "ROLE_USER")));

        assertThatThrownBy(() -> authService.login(new AuthDTOs.LoginRequest("joao@email.com", "errada")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginFailsForUnknownUser() {
        when(userClient.findByEmail("x@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthDTOs.LoginRequest("x@email.com", "senha1234")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void registerRejectsShortPasswordBeforeCallingUserService() {
        var request = new AuthDTOs.RegisterRequest("João Silva", "81999999999", "joao@email.com", "1234");

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(InvalidPasswordException.class);

        verify(userClient, never()).create(any());
    }

    @Test
    void registerSendsHashedPasswordNeverPlaintext() {
        when(userClient.create(any())).thenReturn(
                new CreatedUser(UUID.randomUUID(), "João Silva", "joao@email.com", "ROLE_ADMIN"));

        var response = authService.register(
                new AuthDTOs.RegisterRequest("João Silva", "81999999999", "joao@email.com", "senha1234"));

        ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);
        verify(userClient).create(captor.capture());
        assertThat(captor.getValue().passwordHash()).isNotEqualTo("senha1234");
        assertThat(encoder.matches("senha1234", captor.getValue().passwordHash())).isTrue();
        assertThat(response.token()).isNotBlank();
    }
}
