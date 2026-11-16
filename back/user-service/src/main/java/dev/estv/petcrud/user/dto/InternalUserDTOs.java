package dev.estv.petcrud.user.dto;

import java.util.UUID;

/** Contrato da API INTERNA (/internal/**), consumida por auth-service e pet-service. */
public class InternalUserDTOs {

    /** Enviado pelo auth-service no registro: a senha já chega com hash. */
    public record CreateUserRequest(String name, String number, String email, String passwordHash) {
    }

    public record CreatedUserResponse(UUID id, String name, String email, String role) {
    }

    /** Usado pelo login. É o único lugar que expõe o hash, e só na rede interna. */
    public record CredentialsResponse(UUID id, String name, String email, String password, String role) {
    }

    /** Dados mínimos de dono que o pet-service exibe junto de cada pet. */
    public record UserSummary(UUID id, String name, String number) {
    }
}
