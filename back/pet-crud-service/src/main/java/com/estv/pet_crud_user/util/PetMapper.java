package com.estv.pet_crud_user.util;

import com.estv.pet_crud_user.dto.PetDTOs;
import com.estv.pet_crud_user.entity.PetModel;
import com.estv.pet_crud_user.entity.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(target = "id", expression = "java(pet.getId().toString())")
    @Mapping(target = "type", expression = "java(pet.getType().name())")
    @Mapping(target = "gender", expression = "java(pet.getGender().name())")
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "imageUrl", source = "imageUrl")
    PetDTOs.PetResponse toDTO(PetModel pet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "pet.name")
    @Mapping(target = "owner", source = "user")
    @Mapping(target = "type", expression = "java(PetModel.Type.fromString(pet.type()))")
    @Mapping(target = "gender", expression = "java(PetModel.Gender.fromString(pet.gender()))")
    PetModel toEntity(PetDTOs.PetRecord pet, UserModel user);
}