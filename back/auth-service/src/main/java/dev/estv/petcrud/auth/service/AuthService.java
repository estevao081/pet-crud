package dev.estv.petcrud.auth.service;

import dev.estv.petcrud.auth.client.UserClient;
import dev.estv.petcrud.auth.dto.AuthDTOs;
import dev.estv.petcrud.auth.dto.UserClientDTOs.CreateUserRequest;
import dev.estv.petcrud.auth.dto.UserClientDTOs.CreatedUser;
import dev.estv.petcrud.auth.dto.UserClientDTOs.UserCredentials;
import dev.estv.petcrud.auth.exception.InvalidCredentialsException;
import dev.estv.petcrud.auth.exception.InvalidPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserClient userClient, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public AuthDTOs.LoginResponse login(AuthDTOs.LoginRequest request) {
        if (isBlank(request.email()) || isBlank(request.password())) {
            throw new InvalidCredentialsException();
        }

        UserCredentials user = userClient.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new InvalidCredentialsException();
        }

        return new AuthDTOs.LoginResponse(user.name(), tokenService.generateToken(user.id().toString(), user.role()));
    }

    public AuthDTOs.LoginResponse register(AuthDTOs.RegisterRequest request) {
        // A senha é validada AQUI, em texto puro, antes do hash (no monólito a checagem rodava depois
        // do BCrypt, então nunca falhava). O user-service valida nome, e-mail e telefone.
        if (request.password().length() < 8) {
            throw new InvalidPasswordException();
        }

        CreatedUser created = userClient.create(new CreateUserRequest(
                request.name(),
                request.number(),
                request.email(),
                passwordEncoder.encode(request.password())));

        return new AuthDTOs.LoginResponse(request.name(),
                tokenService.generateToken(created.id().toString(), created.role()));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
