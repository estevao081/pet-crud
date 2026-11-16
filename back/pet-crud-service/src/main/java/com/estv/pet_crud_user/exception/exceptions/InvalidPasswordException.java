package com.estv.pet_crud_user.exception.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Password must have at least 8 characters");
    }
    public InvalidPasswordException(String message) {
        super(message);
    }
}
