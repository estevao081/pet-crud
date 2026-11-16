package dev.estv.petcrud.pet.exception;

public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException() {
        super("Pet not found");
    }
}
