package dev.estv.petcrud.pet.exception;

public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException() {
        super("Invalid address.");
    }
}
