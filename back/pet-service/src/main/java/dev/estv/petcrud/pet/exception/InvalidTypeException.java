package dev.estv.petcrud.pet.exception;

public class InvalidTypeException extends RuntimeException {
    public InvalidTypeException() {
        super("Invalid Type");
    }
}
