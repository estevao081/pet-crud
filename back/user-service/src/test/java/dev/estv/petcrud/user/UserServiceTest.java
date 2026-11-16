package dev.estv.petcrud.user;

import dev.estv.petcrud.user.client.PetClient;
import dev.estv.petcrud.user.dto.InternalUserDTOs;
import dev.estv.petcrud.user.entity.UserEntity;
import dev.estv.petcrud.user.exception.PetServiceUnavailableException;
import dev.estv.petcrud.user.exception.UserAlreadyExistsException;
import dev.estv.petcrud.user.repository.UserRepository;
import dev.estv.petcrud.user.service.UserService;
import dev.estv.petcrud.user.util.UserMapper;
import dev.estv.petcrud.user.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PetClient petClient;

    private UserService userService;

    private final InternalUserDTOs.CreateUserRequest request = new InternalUserDTOs.CreateUserRequest(
            "João Silva", "81912345678", "joao@email.com", "$2a$hash");

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, new ValidationUtil(), userMapper, petClient);
    }

    @Test
    void firstUserBecomesAdmin() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(userService.create(request).getRole()).isEqualTo(UserEntity.Role.ROLE_ADMIN);
    }

    @Test
    void nextUsersAreRegularUsers() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.count()).thenReturn(3L);
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(userService.create(request).getRole()).isEqualTo(UserEntity.Role.ROLE_USER);
    }

    @Test
    void duplicateEmailIsRejected() {
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request)).isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteRemovesPetsBeforeTheUser() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        assertThat(userService.delete(id)).isTrue();

        InOrder order = inOrder(petClient, userRepository);
        order.verify(petClient).deletePetsOwnedBy(id);
        order.verify(userRepository).deleteById(id);
    }

    @Test
    void userIsKeptWhenPetServiceFails() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);
        doThrow(new PetServiceUnavailableException(new RuntimeException("down")))
                .when(petClient).deletePetsOwnedBy(id);

        assertThatThrownBy(() -> userService.delete(id)).isInstanceOf(PetServiceUnavailableException.class);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteReturnsFalseForUnknownUser() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThat(userService.delete(id)).isFalse();
        verifyNoInteractions(petClient);
    }
}
