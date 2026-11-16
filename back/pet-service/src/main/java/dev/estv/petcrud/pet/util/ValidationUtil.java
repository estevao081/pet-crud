package dev.estv.petcrud.pet.util;

import dev.estv.petcrud.pet.entity.PetEntity;
import dev.estv.petcrud.pet.exception.*;
import org.springframework.stereotype.Component;

/** Regras de pet, idênticas às do monólito. */
@Component
public class ValidationUtil {

    private static final String NA = "não informado";

    public String normalizeField(String value) {
        return (value == null || value.isBlank()) ? NA : value.toLowerCase();
    }

    public void validatePet(PetEntity pet) {
        if (!pet.getName().matches("^[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)+$")
                || pet.getName().length() > 40) {
            throw new InvalidNameException();
        }

        if (pet.getType() == null) throw new InvalidTypeException();
        if (pet.getGender() == null) throw new InvalidGenderException();

        if (pet.getCity().isBlank()
                || pet.getCity().length() > 40
                || pet.getState().isBlank()) {
            throw new InvalidAddressException();
        }

        // Pode ter apenas um número de dois dígitos entre 1 e 30
        if (!pet.getAge().matches("^(?:$|[1-9]|[1-2]\\d|30|não informado)$")) {
            throw new InvalidAgeException();
        }

        // Pode ter apenas um número de dois dígitos entre 1 e 90
        if (!pet.getWeight().matches("^(?:$|[1-9]|[1-8]\\d|90|não informado)$")) {
            throw new InvalidWeightException();
        }

        if (pet.getRace().length() > 20) {
            throw new InvalidRaceException();
        }
    }
}
