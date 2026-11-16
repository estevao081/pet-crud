package dev.estv.petcrud.pet.exception;

public class InvalidWeightException extends RuntimeException {
    public InvalidWeightException() {
        super("Weight must be between 1 and 90");
    }
}
