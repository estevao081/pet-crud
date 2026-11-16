package dev.estv.petcrud.pet.util;

import dev.estv.petcrud.pet.dto.PetDTOs;
import dev.estv.petcrud.pet.dto.UserSummary;
import dev.estv.petcrud.pet.entity.PetEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper manual (antes era MapStruct). O dono agora vem de OUTRO serviço, então o mapeamento
 * recebe o UserSummary como parâmetro explícito, o que é mais claro do que forçar o MapStruct.
 */
@Component
public class PetMapper {

    /** @param owner pode ser null (user-service indisponível ou usuário removido): o pet é devolvido sem dono. */
    public PetDTOs.PetResponse toDTO(PetEntity pet, UserSummary owner) {
        PetDTOs.PetResponse dto = new PetDTOs.PetResponse();
        dto.setId(pet.getId().toString());
        dto.setName(pet.getName());
        dto.setType(pet.getType().name());
        dto.setGender(pet.getGender().name());
        dto.setCity(pet.getCity());
        dto.setState(pet.getState());
        dto.setAge(pet.getAge());
        dto.setWeight(pet.getWeight());
        dto.setRace(pet.getRace());
        dto.setImageUrl(pet.getImageUrl());

        if (owner != null) {
            PetDTOs.PetResponse.OwnerDTO ownerDto = new PetDTOs.PetResponse.OwnerDTO();
            ownerDto.setName(owner.name());
            ownerDto.setNumber(parseNumber(owner.number()));
            dto.setOwner(ownerDto);
        }
        return dto;
    }

    public PetEntity toEntity(PetDTOs.PetRecord dto, UUID ownerId) {
        PetEntity pet = new PetEntity();
        pet.setName(dto.name());
        pet.setType(PetEntity.Type.fromString(dto.type()));
        pet.setGender(PetEntity.Gender.fromString(dto.gender()));
        pet.setCity(dto.city());
        pet.setState(dto.state());
        pet.setAge(dto.age());
        pet.setWeight(dto.weight());
        pet.setRace(dto.race());
        pet.setOwnerId(ownerId);
        return pet;
    }

    private long parseNumber(String number) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException | NullPointerException e) {
            return 0L;
        }
    }
}
