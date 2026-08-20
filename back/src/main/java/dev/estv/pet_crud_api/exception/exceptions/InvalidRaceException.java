package dev.estv.pet_crud_api.exception.exceptions;

public class InvalidRaceException extends RuntimeException {
    public InvalidRaceException() {
        super("Invalid Race");
    }

    public InvalidRaceException(String message) {
        super(message);
    }
}
