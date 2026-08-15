package dev.estv.pet_crud_api.controller;

import dev.estv.pet_crud_api.dto.ApiResponse;
import dev.estv.pet_crud_api.dto.AuthDTOs;
import dev.estv.pet_crud_api.dto.UserDTOs;
import dev.estv.pet_crud_api.entity.UserEntity;
import dev.estv.pet_crud_api.security.TokenService;
import dev.estv.pet_crud_api.service.UserService;
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
    private final UserService userService;

    public AuthController(PasswordEncoder passwordEncoder,
                          TokenService tokenService,
                          UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> login(
            @Valid @RequestBody AuthDTOs.LoginRequest dto) {

        UserEntity user = userService.login(dto)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, null, "Invalid credentials"));
        }

        String token = tokenService.generateToken(user);

        return ResponseEntity.status(200).body(
                new ApiResponse<>(
                        true,
                        new AuthDTOs.LoginResponse(user.getName(), token),
                        "Login successful"
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDTOs.LoginResponse>> register(
            @Valid @RequestBody UserDTOs.UserRecord dto) {

        if (userService.findByEmail(dto).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, null, "User already exists"));
        }

        UserEntity newUser = userService.save(dto);
        String token = tokenService.generateToken(newUser);

        return ResponseEntity.status(201)
                .body(new ApiResponse<>(
                        true,
                        new AuthDTOs.LoginResponse(dto.name(), token),
                        "User created successfully"
                ));
    }
}
