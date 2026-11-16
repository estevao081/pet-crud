package dev.estv.petcrud.pet.exception;

public class InvalidNameException extends RuntimeException {
    public InvalidNameException() {
        super("First and last name is required");
    }
}
