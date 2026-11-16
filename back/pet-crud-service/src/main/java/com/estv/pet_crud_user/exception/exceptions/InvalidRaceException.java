package com.estv.pet_crud_user.exception.exceptions;

public class InvalidRaceException extends RuntimeException {
    public InvalidRaceException() {
        super("Invalid Race");
    }

    public InvalidRaceException(String message) {
        super(message);
    }
}
