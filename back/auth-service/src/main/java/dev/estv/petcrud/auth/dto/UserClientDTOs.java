package dev.estv.petcrud.auth.dto;

import java.util.UUID;

/** Contrato com a API interna do user-service. */
public class UserClientDTOs {

    /** Dados de login do usuário (inclui o hash da senha: só trafega na rede interna). */
    public record UserCredentials(UUID id, String name, String email, String password, String role) {
    }

    public record CreateUserRequest(String name, String number, String email, String passwordHash) {
    }

    public record CreatedUser(UUID id, String name, String email, String role) {
    }
}
