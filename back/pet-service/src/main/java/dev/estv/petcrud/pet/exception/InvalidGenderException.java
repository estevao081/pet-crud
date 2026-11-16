package dev.estv.petcrud.pet.exception;

public class InvalidGenderException extends RuntimeException {
    public InvalidGenderException() {
        super("Invalid Gender");
    }
}
