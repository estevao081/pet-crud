package dev.estv.petcrud.user.util;

import dev.estv.petcrud.user.entity.UserEntity;
import dev.estv.petcrud.user.exception.InvalidEmailException;
import dev.estv.petcrud.user.exception.InvalidNameException;
import dev.estv.petcrud.user.exception.InvalidNumberException;
import org.springframework.stereotype.Component;

/** Regras do monólito, menos a de senha: o password agora é responsabilidade do auth-service. */
@Component
public class ValidationUtil {

    public void validateUser(UserEntity user) {
        if (user.getName() == null
                || !user.getName().matches("^[A-Za-zÀ-ÿ]+(?:\\s+[A-Za-zÀ-ÿ]+)+$")
                || user.getName().length() > 40) {
            throw new InvalidNameException();
        }

        if (user.getEmail() == null
                || !user.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException();
        }

        if (user.getNumber() == null || !user.getNumber().matches("\\d{11}")) {
            throw new InvalidNumberException();
        }
    }
}
