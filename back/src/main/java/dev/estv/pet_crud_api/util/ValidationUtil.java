package dev.estv.pet_crud_api.util;

import dev.estv.pet_crud_api.entity.UserModel;
import dev.estv.pet_crud_api.exception.exceptions.*;
import dev.estv.pet_crud_api.entity.PetModel;
import org.springframework.stereotype.Component;

@Component
public class ValidationUtil {

    private static final String NA = "não informado";

    public String normalizeField(String value) {
        return (value == null || value.isBlank()) ? NA : value.toLowerCase();
    }

    public void validatePet(PetModel petModel) {
        if (!petModel.getName().matches("^[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)+$")
                || petModel.getName().length() > 40) {
            throw new InvalidNameException();
        }

        if (petModel.getType() == null) throw new InvalidTypeException();
        if (petModel.getGender() == null) throw new InvalidGenderException();

        if (petModel.getCity().isBlank()
                || petModel.getCity().length() > 40
                || petModel.getState().isBlank()) {
            throw new InvalidAddressException();
        }

        // Pode ter apenas um número de dois dígitos entre 1 e 30
        if (!petModel.getAge().matches("^(?:$|[1-9]|[1-2]\\d|30|não informado)$")) {
            throw new InvalidAgeException();
        }

        // Pode ter apenas um número de dois dígitos entre 1 e 90
        if (!petModel.getWeight().matches("^(?:$|[1-9]|[1-8]\\d|90|não informado)$")) {
            throw new InvalidWeightException();
        }

        if (petModel.getRace().length() > 20) {
            throw new InvalidRaceException();
        }
    }

    public void validateUser(UserModel user) {
        if (!user.getName().matches("^[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)+$")
                || user.getName().length() > 40) {
            throw new InvalidNameException();
        }

        if (!user.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException();
        }

        if (user.getPassword().length() < 8) {
            throw new InvalidPasswordException();
        }

        if (!user.getNumber().matches("\\d{11}")) {
            throw new InvalidNumberException();
        }
    }
}
