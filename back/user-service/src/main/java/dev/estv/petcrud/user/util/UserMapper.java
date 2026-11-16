package dev.estv.petcrud.user.util;

import dev.estv.petcrud.user.dto.UserDTOs;
import dev.estv.petcrud.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTOs.UserResponse toDTO(UserEntity user);
}
