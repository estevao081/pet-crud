package dev.estv.pet_crud_api.util;

import dev.estv.pet_crud_api.dto.UserDTOs;
import dev.estv.pet_crud_api.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTOs.UserResponse toDTO(UserEntity user);
}
