package dev.estv.petcrud.user.exception;

import dev.estv.petcrud.user.dto.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidNameException.class, InvalidEmailException.class,
            InvalidNumberException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(UserAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler(PetServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnavailable(PetServiceUnavailableException ex) {
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
