package dev.estv.pet_crud_api.exception.exceptions;

public class InvalidImageException extends RuntimeException{

    public InvalidImageException() {super("Select an image with less than 5MB or lower resolution");}

    public InvalidImageException(String message) {super(message);}
}
