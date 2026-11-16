package com.estv.pet_crud_user.service;

import com.estv.pet_crud_user.dto.UserDTOs;
import com.estv.pet_crud_user.entity.UserModel;
import com.estv.pet_crud_user.repository.UserRepository;
import com.estv.pet_crud_user.util.UserMapper;
import com.estv.pet_crud_user.util.ValidationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, ValidationUtil validationUtil,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.validationUtil = validationUtil;
        this.userMapper = userMapper;
    }

    public UserModel save(UserDTOs.UserRecord dto) {

        UserModel newUser = new UserModel();
        newUser.setId(UUID.fromString(dto.id()));
        newUser.setName(dto.name());
        newUser.setNumber(dto.number());
        newUser.setAddress(dto.address());

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
        userModel.setAddress(dto.address());

        validationUtil.validateUser(userModel);
        return userRepository.save(userModel);
    }

//    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTOs.UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public UserModel findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
}
