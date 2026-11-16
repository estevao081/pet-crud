package com.estv.pet_crud_auth.service;

import com.estv.pet_crud_auth.dto.ApiResponse;
import com.estv.pet_crud_auth.dto.AuthDTOs;
import com.estv.pet_crud_auth.entity.UserCredential;
import com.estv.pet_crud_auth.repository.UserCredentialRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestClient userServiceClient;
    private final TokenService tokenService;

    public AuthService(UserCredentialRepository userCredentialRepository, PasswordEncoder passwordEncoder, RestClient userServiceClient, TokenService tokenService) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.userServiceClient = userServiceClient;
        this.tokenService = tokenService;
    }

    public Optional<UserCredential> login(AuthDTOs.LoginRequest dto) {
        return userCredentialRepository.findByEmail(dto.email());
    }

    public UserCredential save(AuthDTOs.RegisterRequest dto) {
        UUID userId = UUID.randomUUID();

        UserCredential user = new UserCredential();
        user.setUserId(userId);
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        if (userCredentialRepository.count() == 0) {
            user.setRole(UserCredential.Role.ROLE_ADMIN);
        } else {
            user.setRole(UserCredential.Role.ROLE_USER);
        }

        UserCredential savedUser = userCredentialRepository.save(user);

        saveUserService(dto, userId);

        return savedUser;
    }

    public Optional<UserCredential> findByEmail(AuthDTOs.RegisterRequest dto) {
        return userCredentialRepository.findByEmail(dto.email());
    }

    public AuthDTOs.UserResponse getUser(UUID userId) {

        String serviceToken = tokenService.generateServiceToken();

        ApiResponse<AuthDTOs.UserResponse> response = userServiceClient
                .get()
                .uri("/users/{id}", userId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + serviceToken
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                ApiResponse<AuthDTOs.UserResponse>
                                >() {}
                );

        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "User not found in User Service"
            );
        }

        return response.getData();
    }

    public void saveUserService(AuthDTOs.RegisterRequest dto, UUID userId) {

        AuthDTOs.UserResponse userRequest = new AuthDTOs.UserResponse(
                userId,
                dto.name(),
                dto.number(),
                dto.address()
        );

        userServiceClient
                .post()
                .uri("/users")
                .body(userRequest)
                .retrieve()
                .toBodilessEntity();
    }
}
