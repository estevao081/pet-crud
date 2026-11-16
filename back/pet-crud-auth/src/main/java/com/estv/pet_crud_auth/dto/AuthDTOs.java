package com.estv.pet_crud_auth.dto;

import com.estv.pet_crud_auth.entity.UserCredential;

import java.util.UUID;

public class AuthDTOs {
    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String name, String token) {}
    public record RegisterRequest(
            UUID id,
            String name,
            String number,
            String address,
            String email,
            String password,
            UserCredential.Role role) {}
    public record UserResponse(
            UUID id,
            String name,
            String number,
            String address
    ) {}
}
