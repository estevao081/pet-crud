package dev.estv.petcrud.pet.exception;

public class InvalidAgeException extends RuntimeException {
    public InvalidAgeException() {
        super("Age must be between 1 and 30");
    }
}
