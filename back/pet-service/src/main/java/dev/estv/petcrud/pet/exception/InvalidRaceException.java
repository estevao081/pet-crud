package dev.estv.petcrud.pet.exception;

public class InvalidRaceException extends RuntimeException {
    public InvalidRaceException() {
        super("Invalid Race");
    }
}
