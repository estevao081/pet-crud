package com.estv.pet_crud_user.exception.exceptions;

public class InvalidWeightException extends RuntimeException {
    public InvalidWeightException() {
        super("Weight must be between 1 and 90");
    }

    public InvalidWeightException(String message) {
        super(message);
    }
}
