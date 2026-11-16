package dev.estv.petcrud.auth.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Password must have at least 8 characters");
    }
}
