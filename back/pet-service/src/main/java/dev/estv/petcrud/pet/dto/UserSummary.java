package dev.estv.petcrud.pet.dto;

import java.util.UUID;

/** Visão mínima de um usuário, devolvida pela API interna do user-service. */
public record UserSummary(UUID id, String name, String number) {
}
