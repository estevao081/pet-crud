package dev.estv.petcrud.user.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException() {
        super("Invalid email");
    }
}
