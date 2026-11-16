package com.estv.pet_crud_user.exception.exceptions;

public class InvalidNameException extends RuntimeException {
    public InvalidNameException() {
        super("First and last name is required");
    }
    public InvalidNameException(String message) {
        super(message);
    }
}
