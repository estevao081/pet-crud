package com.estv.pet_crud_auth.controller;

import com.estv.pet_crud_auth.dto.ApiResponse;
import com.estv.pet_crud_auth.dto.AuthDTOs;
import com.estv.pet_crud_auth.entity.UserCredential;
import com.estv.pet_crud_auth.service.AuthService;
import com.estv.pet_crud_auth.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthService authService;

    public AuthController(PasswordEncoder passwordEncoder,
                          TokenService tokenService,
                          AuthService authService) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> login(
            @Valid @RequestBody AuthDTOs.LoginRequest dto) {

        var userOptional = authService.login(dto);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, null, "Invalid credentials"));
        }

        UserCredential userCredential = userOptional.get();

        if (!passwordEncoder.matches(dto.password(), userCredential.getPassword())) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, null, "Invalid credentials"));
        }

        AuthDTOs.UserResponse userResponse = authService.getUser(userCredential.getUserId());

        String token = tokenService.generateToken(userCredential);

        return ResponseEntity.status(200).body(
                new ApiResponse<>(
                        true,
                        new AuthDTOs.LoginResponse(userResponse.name(), token),
                        "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> register(
            @Valid @RequestBody AuthDTOs.RegisterRequest dto) {

        if (authService.findByEmail(dto).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "User already exists"));
        }

        UserCredential newUser = authService.save(dto);
        String token = tokenService.generateToken(newUser);

        return ResponseEntity.status(201)
                .body(new ApiResponse<>(
                        true,
                        new AuthDTOs.LoginResponse(dto.name(), token),
                        "User created successfully"));
    }
}

