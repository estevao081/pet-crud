package dev.estv.petcrud.user.dto;

import java.time.LocalDateTime;

/** Mesmo envelope do monólito (o frontend depende dele). Duplicado em cada serviço de propósito. */
public record ApiResponse<T>(boolean success, T data, String message, LocalDateTime timestamp) {

    public ApiResponse(boolean success, T data, String message) {
        this(success, data, message, LocalDateTime.now());
    }
}
