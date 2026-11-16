package dev.estv.pet_crud_api.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private LocalDateTime timestamp;

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
