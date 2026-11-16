package com.estv.pet_crud_user.exception.exceptions;

public class InvalidAgeException extends RuntimeException {
    public InvalidAgeException() {
        super("Age must be between 1 and 30");
    }

    public InvalidAgeException(String message) {
        super(message);
    }
}
