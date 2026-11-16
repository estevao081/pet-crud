package com.estv.pet_crud_user.exception.exceptions;

public class InvalidGenderException extends RuntimeException {

    public InvalidGenderException() {
        super("Invalid Gender");
    }

    public InvalidGenderException(String message) {
        super(message);
    }
}
