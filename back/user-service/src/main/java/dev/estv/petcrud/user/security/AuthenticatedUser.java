package dev.estv.petcrud.user.security;

import java.util.UUID;

/** Identidade repassada pelo gateway (headers X-User-Id / X-User-Role). */
public record AuthenticatedUser(UUID id, String role) {
}
