package dev.estv.petcrud.auth.controller;

import dev.estv.petcrud.auth.dto.ApiResponse;
import dev.estv.petcrud.auth.dto.AuthDTOs;
import dev.estv.petcrud.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> login(@RequestBody AuthDTOs.LoginRequest dto) {
        return ResponseEntity.ok(new ApiResponse<>(true, authService.login(dto), "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> register(
            @Valid @RequestBody AuthDTOs.RegisterRequest dto) {
        return ResponseEntity.status(201)
                .body(new ApiResponse<>(true, authService.register(dto), "User created successfully"));
    }
}
