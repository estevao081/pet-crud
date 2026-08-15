package dev.estv.pet_crud_api.service;

import dev.estv.pet_crud_api.dto.AuthDTOs;
import dev.estv.pet_crud_api.dto.UserDTOs;
import dev.estv.pet_crud_api.entity.UserEntity;
import dev.estv.pet_crud_api.repository.UserRepository;
import dev.estv.pet_crud_api.util.UserMapper;
import dev.estv.pet_crud_api.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Testes Unitários")
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "adminEmail", "admin@pets.com");
    }

    private UserEntity buildUserModel(String name, String email) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded_password");
        user.setNumber("81900000000");
        user.setRole(UserEntity.Role.ROLE_USER);
        return user;
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("Deve salvar usuário comum com role ROLE_USER")
        void shouldSaveRegularUserWithRoleUser() {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord(
                    "João Silva", "81900000000", "joao@email.com", "senha1234"
            );
            when(passwordEncoder.encode(dto.password())).thenReturn("encoded_password");
            when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            UserEntity saved = userService.save(dto);

            assertThat(saved.getRole()).isEqualTo(UserEntity.Role.ROLE_USER);
            assertThat(saved.getEmail()).isEqualTo("joao@email.com");
            verify(validationUtil).validateUser(any(UserEntity.class));
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Deve salvar admin com role ROLE_ADMIN quando email coincide")
        void shouldSaveAdminWithRoleAdmin() {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord(
                    "Admin Master", "81900000000", "admin@pets.com", "senha1234"
            );
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserEntity saved = userService.save(dto);

            assertThat(saved.getRole()).isEqualTo(UserEntity.Role.ROLE_ADMIN);
        }

        @Test
        @DisplayName("Deve encodar a senha antes de salvar")
        void shouldEncodePasswordBeforeSaving() {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord(
                    "João Silva", "81900000000", "joao@email.com", "senha1234"
            );
            when(passwordEncoder.encode("senha1234")).thenReturn("bcrypt_encoded");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserEntity saved = userService.save(dto);

            assertThat(saved.getPassword()).isEqualTo("bcrypt_encoded");
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deve retornar true e deletar quando usuário existe")
        void shouldReturnTrueAndDeleteWhenUserExists() {
            UUID id = UUID.randomUUID();
            when(userRepository.existsById(id)).thenReturn(true);

            boolean result = userService.delete(id);

            assertThat(result).isTrue();
            verify(userRepository).deleteById(id);
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não existe")
        void shouldReturnFalseWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.existsById(id)).thenReturn(false);

            boolean result = userService.delete(id);

            assertThat(result).isFalse();
            verify(userRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("findByEmail() e login()")
    class FindAndLogin {

        @Test
        @DisplayName("findByEmail() deve retornar Optional com user quando existe")
        void shouldReturnUserWhenEmailExists() {
            UserEntity user = buildUserModel("João Silva", "joao@email.com");
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord("João Silva", "81900000000", "joao@email.com", "senha1234");
            when(userRepository.findByUsermail("joao@email.com")).thenReturn(Optional.of(user));

            Optional<UserEntity> result = userService.findByEmail(dto);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("joao@email.com");
        }

        @Test
        @DisplayName("findByEmail() deve retornar Optional vazio quando não existe")
        void shouldReturnEmptyWhenEmailNotFound() {
            UserDTOs.UserRecord dto = new UserDTOs.UserRecord("João Silva", "81900000000", "naoexiste@email.com", "senha1234");
            when(userRepository.findByUsermail("naoexiste@email.com")).thenReturn(Optional.empty());

            Optional<UserEntity> result = userService.findByEmail(dto);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("login() deve retornar usuário pelo email")
        void shouldReturnUserByEmail() {
            UserEntity user = buildUserModel("João Silva", "joao@email.com");
            AuthDTOs.LoginRequest dto = new AuthDTOs.LoginRequest("joao@email.com", "senha1234");
            when(userRepository.findByUsermail("joao@email.com")).thenReturn(Optional.of(user));

            Optional<UserEntity> result = userService.login(dto);

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de UserResponseDTO")
        void shouldReturnListOfUserDTOs() {
            UserEntity user1 = buildUserModel("João Silva", "joao@email.com");
            UserEntity user2 = buildUserModel("Maria Costa", "maria@email.com");

            UserDTOs.UserResponse dto1 = new UserDTOs.UserResponse(user1.getId().toString(), "João Silva", "81900000000", "joao@email.com", "ROLE_USER");
            UserDTOs.UserResponse dto2 = new UserDTOs.UserResponse(user2.getId().toString(), "Maria Costa", "81900000001", "maria@email.com", "ROLE_USER");

            when(userRepository.findAll()).thenReturn(List.of(user1, user2));
            when(userMapper.toDTO(user1)).thenReturn(dto1);
            when(userMapper.toDTO(user2)).thenReturn(dto2);

            List<UserDTOs.UserResponse> result = userService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).email()).isEqualTo("joao@email.com");
        }
    }
}