package com.estv.pet_crud_user.exception.exceptions;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException() {
        super("Invalid email");
    }
    public InvalidEmailException(String message) {
        super(message);
    }
}
