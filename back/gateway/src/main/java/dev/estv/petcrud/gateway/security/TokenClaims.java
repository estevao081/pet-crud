package dev.estv.petcrud.gateway.security;

/** Identidade extraída de um JWT válido. */
public record TokenClaims(String userId, String role) {
}
