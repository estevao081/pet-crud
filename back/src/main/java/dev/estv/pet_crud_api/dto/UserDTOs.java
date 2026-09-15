package dev.estv.pet_crud_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDTOs {

    public record UserRecord(
            @NotNull
            @NotBlank(message = "Name cannot be blank")
            String name,
            @NotBlank(message= "Phone number cannot be blank")
            @NotNull
            String number,
            @NotNull
            @NotBlank(message = "E-mail cannot be blank")
            String email,
            @NotNull
            @NotBlank(message = "Password cannot be blank")
            String password,
            String address
    ){}

    public record UserResponse (
            String id,
            String name,
            String number,
            String email,
            String role,
            String address
    ){}

    public record UserUpdate (
            @NotNull
            @NotBlank(message = "Name cannot be blank")
            String name,
            @NotBlank(message = "Phone number cannot be blank")
            @NotNull
            String number,
            @NotNull
            @NotBlank(message = "E-mail cannot be blank")
            String email,
            String address
    ){}
}
