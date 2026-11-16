package com.estv.pet_crud_user.exception.handler;

import com.estv.pet_crud_user.dto.ApiResponse;
import com.estv.pet_crud_user.exception.exceptions.*;
import com.estv.pet_crud_user.exception.exceptions.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        var errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, errors, "Validation Error"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxSizeException(
            MaxUploadSizeExceededException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        false,
                        null,
                        "The image exceeds the maximum allowed size"));
    }

    @ExceptionHandler({
            InvalidTypeException.class,
            InvalidGenderException.class,
            InvalidNameException.class,
            InvalidAddressException.class,
            InvalidAgeException.class,
            InvalidWeightException.class,
            InvalidRaceException.class,
            InvalidEmailException.class,
            InvalidPasswordException.class,
            InvalidNumberException.class,
            InvalidImageException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusiness(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, null, ex.getMessage()));
    }
}
