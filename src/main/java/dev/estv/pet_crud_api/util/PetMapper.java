package dev.estv.pet_crud_api.util;

import dev.estv.pet_crud_api.dto.PetDTOs;
import dev.estv.pet_crud_api.entity.PetEntity;
import dev.estv.pet_crud_api.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(target = "id", expression = "java(pet.getId().toString())")
    @Mapping(target = "type", expression = "java(pet.getType().name())")
    @Mapping(target = "gender", expression = "java(pet.getGender().name())")
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "imageUrl", source = "imageUrl")
    PetDTOs.PetResponse toDTO(PetEntity pet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "pet.name")
    @Mapping(target = "owner", source = "user")
    @Mapping(target = "type", expression = "java(PetModel.Type.fromString(pet.type()))")
    @Mapping(target = "gender", expression = "java(PetModel.Gender.fromString(pet.gender()))")
    PetEntity toEntity(PetDTOs.PetRecord pet, UserEntity user);
}