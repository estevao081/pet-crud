package dev.estv.petcrud.user.exception;

public class InvalidNumberException extends RuntimeException {
    public InvalidNumberException() {
        super("Number must contains exactly 11 digits (Ex: 81999999999)");
    }
}
