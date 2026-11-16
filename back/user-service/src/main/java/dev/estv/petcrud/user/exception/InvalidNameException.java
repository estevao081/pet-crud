package dev.estv.petcrud.user.exception;

public class InvalidNameException extends RuntimeException {
    public InvalidNameException() {
        super("First and last name is required");
    }
}
