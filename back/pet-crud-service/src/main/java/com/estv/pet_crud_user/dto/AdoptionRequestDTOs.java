package com.estv.pet_crud_user.dto;

import java.time.LocalDateTime;

public class AdoptionRequestDTOs {

    public record AdoptionRequestResponse(
            String id,
            String petId,
            String petName,
            String petImageUrl,
            String requesterId,
            String requesterName,
            String requesterPhone,
            String requesterAddress,
            String status,
            LocalDateTime createdAt,
            LocalDateTime respondedAt
    ) {}
}
