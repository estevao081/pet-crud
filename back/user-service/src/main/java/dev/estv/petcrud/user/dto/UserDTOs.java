package dev.estv.petcrud.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTOs da API pública (acessada pelo frontend via gateway). Idênticos aos do monólito. */
public class UserDTOs {

    public record UserResponse(
            String id,
            String name,
            String number,
            String email,
            String role
    ) {
    }

    public record UserUpdate(
            @NotNull @NotBlank(message = "Name cannot be blank") String name,
            @NotNull @NotBlank(message = "Phone number cannot be blank") String number,
            @NotNull @NotBlank(message = "E-mail cannot be blank") String email
    ) {
    }
}
