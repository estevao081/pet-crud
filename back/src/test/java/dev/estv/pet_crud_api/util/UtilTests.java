package dev.estv.pet_crud_api.util;

import dev.estv.pet_crud_api.dto.PetDTOs;
import dev.estv.pet_crud_api.dto.UserDTOs;
import dev.estv.pet_crud_api.entity.PetModel;
import dev.estv.pet_crud_api.entity.UserModel;
import dev.estv.pet_crud_api.exception.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Util - Testes Unitários")
class ValidationUtilTest {

    private ValidationUtil validationUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PetMapper petMapper;

    @BeforeEach
    void setUp() {
        validationUtil = new ValidationUtil();
    }

    @Nested
    @DisplayName("normalizeField()")
    class NormalizeField {

        @Test
        @DisplayName("Deve retornar 'não informado' quando valor é null")
        void shouldReturnNaoInformadoForNull() {
            assertThat(validationUtil.normalizeField(null)).isEqualTo("não informado");
        }

        @Test
        @DisplayName("Deve retornar 'não informado' quando valor é blank")
        void shouldReturnNaoInformadoForBlank() {
            assertThat(validationUtil.normalizeField("   ")).isEqualTo("não informado");
        }

        @Test
        @DisplayName("Deve retornar valor em lowercase quando preenchido")
        void shouldReturnLowercaseValue() {
            assertThat(validationUtil.normalizeField("VIRA-LATA")).isEqualTo("vira-lata");
        }
    }

    @Nested
    @DisplayName("validatePet()")
    class ValidatePet {

        private PetModel buildValidPet() {
            return PetModel.builder()
                    .name("rex caramelo")
                    .type(PetModel.Type.CAO)
                    .gender(PetModel.Gender.M)
                    .city("recife")
                    .state("PE")
                    .age("5")
                    .weight("10")
                    .race("vira-lata")
                    .build();
        }

        @Test
        @DisplayName("Deve validar pet com dados corretos sem lançar exceção")
        void shouldPassForValidPet() {
            assertThatCode(() -> validationUtil.validatePet(buildValidPet()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lançar InvalidNameException para nome sem sobrenome")
        void shouldThrowForNameWithoutLastName() {
            PetModel pet = buildValidPet();
            pet.setName("rex");
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidNameException para nome maior que 40 caracteres")
        void shouldThrowForNameTooLong() {
            PetModel pet = buildValidPet();
            pet.setName("nome muito longo que ultrapassa quarenta caracteres mesmo");
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidTypeException para type null")
        void shouldThrowForNullType() {
            PetModel pet = buildValidPet();
            pet.setType(null);
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidTypeException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidGenderException para gender null")
        void shouldThrowForNullGender() {
            PetModel pet = buildValidPet();
            pet.setGender(null);
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidGenderException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidAddressException para cidade em branco")
        void shouldThrowForBlankCity() {
            PetModel pet = buildValidPet();
            pet.setCity("");
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidAddressException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidAgeException para idade fora do intervalo")
        void shouldThrowForInvalidAge() {
            PetModel pet = buildValidPet();
            pet.setAge("99");
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidAgeException.class);
        }

        @Test
        @DisplayName("Deve aceitar 'não informado' como idade válida")
        void shouldAcceptNaoInformadoAsAge() {
            PetModel pet = buildValidPet();
            pet.setAge("não informado");
            assertThatCode(() -> validationUtil.validatePet(pet)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lançar InvalidWeightException para peso fora do intervalo")
        void shouldThrowForInvalidWeight() {
            PetModel pet = buildValidPet();
            pet.setWeight("100");
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidWeightException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidRaceException para raça com mais de 20 caracteres")
        void shouldThrowForRaceTooLong() {
            PetModel pet = buildValidPet();
            pet.setRace("raça muito longa demais aqui");
            assertThatThrownBy(() -> validationUtil.validatePet(pet))
                    .isInstanceOf(InvalidRaceException.class);
        }
    }

    @Nested
    @DisplayName("validateUser()")
    class ValidateUser {

        private UserModel buildValidUser() {
            UserModel user = new UserModel();
            user.setName("João Silva");
            user.setEmail("joao@email.com");
            user.setPassword("senha1234");
            user.setNumber("81912345678");
            user.setRole(UserModel.Role.ROLE_USER);
            return user;
        }

        @Test
        @DisplayName("Deve validar usuário com dados corretos")
        void shouldPassForValidUser() {
            assertThatCode(() -> validationUtil.validateUser(buildValidUser()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lançar InvalidNameException para nome sem sobrenome")
        void shouldThrowForNameWithoutLastName() {
            UserModel user = buildValidUser();
            user.setName("João");
            assertThatThrownBy(() -> validationUtil.validateUser(user))
                    .isInstanceOf(InvalidNameException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidEmailException para email inválido")
        void shouldThrowForInvalidEmail() {
            UserModel user = buildValidUser();
            user.setEmail("emailinvalido");
            assertThatThrownBy(() -> validationUtil.validateUser(user))
                    .isInstanceOf(InvalidEmailException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidPasswordException para senha com menos de 8 caracteres")
        void shouldThrowForShortPassword() {
            UserModel user = buildValidUser();
            user.setPassword("1234");
            assertThatThrownBy(() -> validationUtil.validateUser(user))
                    .isInstanceOf(InvalidPasswordException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidNumberException para número com formato incorreto")
        void shouldThrowForInvalidNumber() {
            UserModel user = buildValidUser();
            user.setNumber("12345");
            assertThatThrownBy(() -> validationUtil.validateUser(user))
                    .isInstanceOf(InvalidNumberException.class);
        }

        @Test
        @DisplayName("Deve lançar InvalidNumberException para número com letras")
        void shouldThrowForAlphanumericNumber() {
            UserModel user = buildValidUser();
            user.setNumber("8191234abc7");
            assertThatThrownBy(() -> validationUtil.validateUser(user))
                    .isInstanceOf(InvalidNumberException.class);
        }
    }

    @Nested
    @DisplayName("toEntity() e toDTO()")
    class Conversion {

        @Test
        @DisplayName("toEntity() deve mapear DTO para PetModel corretamente")
        void shouldMapDtoToPetModel() {
            PetDTOs.PetRecord dto = new PetDTOs.PetRecord(
                    "Rex Caramelo", "CÃO", "M", "Recife", "PE", "5", "10", "vira-lata"
            );
            UserModel user = new UserModel();
            user.setId(UUID.randomUUID());
            user.setName("João Silva");

            PetModel pet = petMapper.toEntity(dto, user);

            assertThat(pet.getName()).isEqualTo("rex caramelo");
            assertThat(pet.getType()).isEqualTo(PetModel.Type.CAO);
            assertThat(pet.getGender()).isEqualTo(PetModel.Gender.M);
            assertThat(pet.getCity()).isEqualTo("recife");
            assertThat(pet.getState()).isEqualTo("PE");
            assertThat(pet.getOwner()).isEqualTo(user);
            assertThat(pet.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("toDTO() deve mapear UserModel para UserResponseDTO corretamente")
        void shouldMapUserModelToDTO() {
            UserModel user = new UserModel();
            user.setId(UUID.randomUUID());
            user.setName("Maria Souza");
            user.setEmail("maria@email.com");
            user.setNumber("81900000000");
            user.setRole(UserModel.Role.ROLE_ADMIN);

            UserDTOs.UserResponse dto = userMapper.toDTO(user);

            assertThat(dto.name()).isEqualTo("Maria Souza");
            assertThat(dto.email()).isEqualTo("maria@email.com");
            assertThat(dto.role()).isEqualTo("ROLE_ADMIN");
        }
    }
}