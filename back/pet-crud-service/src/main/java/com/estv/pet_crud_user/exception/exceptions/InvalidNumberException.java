package com.estv.pet_crud_user.exception.exceptions;

public class InvalidNumberException extends RuntimeException {
    public InvalidNumberException() {
        super("Number must contains exactly 11 digits (Ex: 81999999999)");
    }
    public InvalidNumberException(String message) {
        super(message);
    }
}
