package dev.estv.petcrud.pet.exception;

public class InvalidImageException extends RuntimeException {
    public InvalidImageException() {
        super("Select an image with less than 5MB or lower resolution");
    }
}
