package dev.estv.pet_crud_api.exception.exceptions;

public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException() {
        super("Invalid address.");
    }
    public InvalidAddressException(String message) {
        super(message);
    }
}
