package com.estv.pet_crud_user.exception.exceptions;

public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException() {
        super("Invalid address.");
    }
    public InvalidAddressException(String message) {
        super(message);
    }
}
