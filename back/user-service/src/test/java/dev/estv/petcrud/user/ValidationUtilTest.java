package dev.estv.petcrud.user;

import dev.estv.petcrud.user.entity.UserEntity;
import dev.estv.petcrud.user.exception.InvalidEmailException;
import dev.estv.petcrud.user.exception.InvalidNameException;
import dev.estv.petcrud.user.exception.InvalidNumberException;
import dev.estv.petcrud.user.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilTest {

    private final ValidationUtil validationUtil = new ValidationUtil();

    private UserEntity valid() {
        UserEntity u = new UserEntity();
        u.setName("João Silva");
        u.setEmail("joao@email.com");
        u.setNumber("81912345678");
        return u;
    }

    @Test
    void acceptsValidUser() {
        assertThatCode(() -> validationUtil.validateUser(valid())).doesNotThrowAnyException();
    }

    @Test
    void rejectsNameWithoutLastName() {
        UserEntity u = valid();
        u.setName("João");
        assertThatThrownBy(() -> validationUtil.validateUser(u)).isInstanceOf(InvalidNameException.class);
    }

    @Test
    void rejectsInvalidEmail() {
        UserEntity u = valid();
        u.setEmail("emailinvalido");
        assertThatThrownBy(() -> validationUtil.validateUser(u)).isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void rejectsNumberThatIsNotElevenDigits() {
        UserEntity u = valid();
        u.setNumber("8191234abc7");
        assertThatThrownBy(() -> validationUtil.validateUser(u)).isInstanceOf(InvalidNumberException.class);
    }
}
