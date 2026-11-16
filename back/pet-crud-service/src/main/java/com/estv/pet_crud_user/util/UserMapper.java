package com.estv.pet_crud_user.util;

import com.estv.pet_crud_user.dto.UserDTOs;
import com.estv.pet_crud_user.entity.UserModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTOs.UserResponse toDTO(UserModel user);
}
