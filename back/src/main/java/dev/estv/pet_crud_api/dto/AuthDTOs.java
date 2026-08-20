package dev.estv.pet_crud_api.dto;

public class AuthDTOs {
    public record LoginRequest(String email, String password) {}
    public record LoginResponse(String name, String token) {}
}
