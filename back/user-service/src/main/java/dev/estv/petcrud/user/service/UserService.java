package dev.estv.petcrud.user.service;

import dev.estv.petcrud.user.client.PetClient;
import dev.estv.petcrud.user.dto.InternalUserDTOs;
import dev.estv.petcrud.user.dto.UserDTOs;
import dev.estv.petcrud.user.entity.UserEntity;
import dev.estv.petcrud.user.exception.UserAlreadyExistsException;
import dev.estv.petcrud.user.exception.UserNotFoundException;
import dev.estv.petcrud.user.repository.UserRepository;
import dev.estv.petcrud.user.util.UserMapper;
import dev.estv.petcrud.user.util.ValidationUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;
    private final UserMapper userMapper;
    private final PetClient petClient;

    public UserService(UserRepository userRepository, ValidationUtil validationUtil,
                       UserMapper userMapper, PetClient petClient) {
        this.userRepository = userRepository;
        this.validationUtil = validationUtil;
        this.userMapper = userMapper;
        this.petClient = petClient;
    }

    /** Chamado pelo auth-service (API interna). O primeiro usuário do sistema vira ADMIN, como antes. */
    public UserEntity create(InternalUserDTOs.CreateUserRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException();
        }

        UserEntity user = new UserEntity();
        user.setName(dto.name());
        user.setNumber(dto.number());
        user.setEmail(dto.email());
        user.setPassword(dto.passwordHash());
        user.setRole(userRepository.count() == 0 ? UserEntity.Role.ROLE_ADMIN : UserEntity.Role.ROLE_USER);

        validationUtil.validateUser(user);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(); // corrida entre dois registros com o mesmo e-mail
        }
    }

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<InternalUserDTOs.UserSummary> findSummaries(Collection<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .map(u -> new InternalUserDTOs.UserSummary(u.getId(), u.getName(), u.getNumber()))
                .toList();
    }

    /**
     * Ordem importa: primeiro os pets, depois o usuário. Se a chamada ao pet-service falhar, o usuário
     * continua existindo e o cliente pode tentar de novo (a remoção de pets é idempotente). A ordem
     * inversa deixaria pets órfãos sem dono se a segunda chamada falhasse.
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public boolean delete(UUID id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        petClient.deletePetsOwnedBy(id);
        userRepository.deleteById(id);
        return true;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDTOs.UserResponse update(UUID id, UserDTOs.UserUpdate dto) {
        UserEntity user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        user.setName(dto.name());
        user.setNumber(dto.number());
        user.setEmail(dto.email());

        validationUtil.validateUser(user);
        return userMapper.toDTO(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTOs.UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toDTO).toList();
    }
}
