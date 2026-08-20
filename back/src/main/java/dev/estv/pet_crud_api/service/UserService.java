package dev.estv.pet_crud_api.service;

import dev.estv.pet_crud_api.dto.AuthDTOs;
import dev.estv.pet_crud_api.dto.UserDTOs;
import dev.estv.pet_crud_api.entity.UserModel;
import dev.estv.pet_crud_api.repository.UserRepository;
import dev.estv.pet_crud_api.util.UserMapper;
import dev.estv.pet_crud_api.util.ValidationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;
    private final UserMapper userMapper;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, ValidationUtil validationUtil,
            UserMapper userMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.validationUtil = validationUtil;
        this.userMapper = userMapper;
    }

    public UserModel save(UserDTOs.UserRecord dto) {

        UserModel newUser = new UserModel();
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.setEmail(dto.email());
        newUser.setName(dto.name());
        newUser.setNumber(dto.number());

        if (userRepository.count() == 0) {
            newUser.setRole(UserModel.Role.ROLE_ADMIN);
        } else {
            newUser.setRole(UserModel.Role.ROLE_USER);
        }

        validationUtil.validateUser(newUser);
        this.userRepository.save(newUser);

        return newUser;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public boolean delete(UUID id) {
        if (!userRepository.existsById(id)) {
            return false;
        }

        userRepository.deleteById(id);
        return true;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UserModel update(UUID id, UserDTOs.UserUpdate dto) {
        Optional<UserModel> user = userRepository.findById(id);
        var userModel = user.get();

        userModel.setName(dto.name());
        userModel.setNumber(dto.number());
        userModel.setEmail(dto.email());

        validationUtil.validateUser(userModel);
        return userRepository.save(userModel);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTOs.UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserModel findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<UserModel> findByEmail(UserDTOs.UserRecord dto) {
        return userRepository.findByUsermail(dto.email());
    }

    public Optional<UserModel> login(AuthDTOs.LoginRequest dto) {
        return userRepository.findByUsermail(dto.email());
    }
}
