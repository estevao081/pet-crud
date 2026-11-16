package dev.estv.petcrud.auth.exception;

import dev.estv.petcrud.auth.dto.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(401).body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler({InvalidPasswordException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler(UserServiceRejectedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRejected(UserServiceRejectedException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnavailable(UserServiceUnavailableException ex) {
        return ResponseEntity.status(503).body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, errors, "Validation Error"));
    }
}
