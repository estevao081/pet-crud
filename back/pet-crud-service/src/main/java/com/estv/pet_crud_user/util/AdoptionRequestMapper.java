package com.estv.pet_crud_user.util;

import com.estv.pet_crud_user.dto.AdoptionRequestDTOs;
import com.estv.pet_crud_user.entity.AdoptionRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdoptionRequestMapper {

    @Mapping(target = "id", expression = "java(model.getId().toString())")
    @Mapping(target = "petId", expression = "java(model.getPet().getId().toString())")
    @Mapping(target = "petName", source = "pet.name")
    @Mapping(target = "petImageUrl", source = "pet.imageUrl")
    @Mapping(target = "requesterId", expression = "java(model.getRequester().getId().toString())")
    @Mapping(target = "status", expression = "java(model.getStatus().name())")
    AdoptionRequestDTOs.AdoptionRequestResponse toDTO(AdoptionRequestModel model);
}
