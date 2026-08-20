package dev.estv.pet_crud_api.exception.exceptions;

public class InvalidTypeException extends RuntimeException {

    public InvalidTypeException() {
        super("Invalid Type");
    }

    public InvalidTypeException(String message) {
        super(message);
    }
}
