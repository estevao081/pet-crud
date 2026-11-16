package dev.estv.petcrud.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthDTOs {

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String name, String token) {
    }

    public record RegisterRequest(
            @NotNull @NotBlank(message = "Name cannot be blank") String name,
            @NotNull @NotBlank(message = "Phone number cannot be blank") String number,
            @NotNull @NotBlank(message = "E-mail cannot be blank") String email,
            @NotNull @NotBlank(message = "Password cannot be blank") String password
    ) {
    }
}
