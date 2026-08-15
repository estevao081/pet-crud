package dev.estv.pet_crud_api.util;

import dev.estv.pet_crud_api.entity.UserEntity;
import dev.estv.pet_crud_api.exception.exceptions.*;
import dev.estv.pet_crud_api.entity.PetEntity;
import org.springframework.stereotype.Component;

@Component
public class ValidationUtil {

    private static final String NA = "não informado";

    public String normalizeField(String value) {
        return (value == null || value.isBlank()) ? NA : value.toLowerCase();
    }

    public void validatePet(PetEntity petEntity) {
        if (!petEntity.getName().matches("^[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)+$")
                || petEntity.getName().length() > 40) {
            throw new InvalidNameException();
        }

        if (petEntity.getType() == null) throw new InvalidTypeException();
        if (petEntity.getGender() == null) throw new InvalidGenderException();

        if (petEntity.getCity().isBlank()
                || petEntity.getCity().length() > 40
                || petEntity.getState().isBlank()) {
            throw new InvalidAddressException();
        }

        if (!petEntity.getAge().matches("^(?:$|[1-9]|[1-2]\\d|30|não informado)$")) {
            throw new InvalidAgeException();
        }

        if (!petEntity.getWeight().matches("^(?:$|[1-9]|[1-8]\\d|90|não informado)$")) {
            throw new InvalidWeightException();
        }

        if (petEntity.getRace().length() > 20) {
            throw new InvalidRaceException();
        }
    }

    public void validateUser(UserEntity user) {
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
