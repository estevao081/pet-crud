package com.estv.pet_crud_user.exception.exceptions;

public class InvalidTypeException extends RuntimeException {

    public InvalidTypeException() {
        super("Invalid Type");
    }

    public InvalidTypeException(String message) {
        super(message);
    }
}
