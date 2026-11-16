package dev.estv.petcrud.user.exception;

public class PetServiceUnavailableException extends RuntimeException {
    public PetServiceUnavailableException(Throwable cause) {
        super("Pet service unavailable. The user was not deleted, try again later", cause);
    }
}
