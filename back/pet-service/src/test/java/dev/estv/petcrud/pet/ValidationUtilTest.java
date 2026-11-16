package dev.estv.petcrud.pet;

import dev.estv.petcrud.pet.entity.PetEntity;
import dev.estv.petcrud.pet.exception.*;
import dev.estv.petcrud.pet.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilTest {

    private final ValidationUtil validationUtil = new ValidationUtil();

    private PetEntity valid() {
        return PetEntity.builder().name("rex caramelo").type(PetEntity.Type.CAO).gender(PetEntity.Gender.M)
                .city("recife").state("PE").age("5").weight("10").race("vira-lata").build();
    }

    @Test
    void acceptsValidPet() {
        assertThatCode(() -> validationUtil.validatePet(valid())).doesNotThrowAnyException();
    }

    @Test
    void rejectsNameWithoutLastName() {
        PetEntity p = valid();
        p.setName("rex");
        assertThatThrownBy(() -> validationUtil.validatePet(p)).isInstanceOf(InvalidNameException.class);
    }

    @Test
    void rejectsAgeOutOfRange() {
        PetEntity p = valid();
        p.setAge("99");
        assertThatThrownBy(() -> validationUtil.validatePet(p)).isInstanceOf(InvalidAgeException.class);
    }

    @Test
    void acceptsNaoInformadoAsAge() {
        PetEntity p = valid();
        p.setAge("não informado");
        assertThatCode(() -> validationUtil.validatePet(p)).doesNotThrowAnyException();
    }

    @Test
    void rejectsWeightOutOfRange() {
        PetEntity p = valid();
        p.setWeight("100");
        assertThatThrownBy(() -> validationUtil.validatePet(p)).isInstanceOf(InvalidWeightException.class);
    }

    @Test
    void rejectsBlankCity() {
        PetEntity p = valid();
        p.setCity("");
        assertThatThrownBy(() -> validationUtil.validatePet(p)).isInstanceOf(InvalidAddressException.class);
    }
}
