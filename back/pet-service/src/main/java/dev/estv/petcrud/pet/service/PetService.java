package dev.estv.petcrud.pet.service;

import dev.estv.petcrud.pet.client.UserClient;
import dev.estv.petcrud.pet.dto.PetDTOs;
import dev.estv.petcrud.pet.dto.UserSummary;
import dev.estv.petcrud.pet.entity.PetEntity;
import dev.estv.petcrud.pet.exception.PetNotFoundException;
import dev.estv.petcrud.pet.repository.PetRepository;
import dev.estv.petcrud.pet.specification.PetSpecification;
import dev.estv.petcrud.pet.util.PetMapper;
import dev.estv.petcrud.pet.util.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final ValidationUtil validationUtil;
    private final UserClient userClient;

    public PetService(PetRepository petRepository, PetMapper petMapper,
                      ValidationUtil validationUtil, UserClient userClient) {
        this.petRepository = petRepository;
        this.petMapper = petMapper;
        this.validationUtil = validationUtil;
        this.userClient = userClient;
    }

    public Page<PetDTOs.PetResponse> listPets(int page, int items) {
        Pageable pageable = PageRequest.of(page, items, Sort.by("createdAt").descending());
        return withOwners(petRepository.findAll(pageable));
    }

    public Page<PetDTOs.PetResponse> search(PetDTOs.PetResponse filter, int page, int items) {
        Pageable pageable = PageRequest.of(page, items, Sort.by("createdAt").descending());
        return withOwners(petRepository.findAll(PetSpecification.filter(filter), pageable));
    }

    /** O dono vem do JWT (via gateway), não precisa mais de consulta ao banco de usuários. */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PetEntity save(PetDTOs.PetRecord dto, String imageUrl, UUID ownerId) {
        PetEntity pet = petMapper.toEntity(dto, ownerId);
        pet.setImageUrl(imageUrl);
        validationUtil.validatePet(pet);
        return petRepository.save(pet);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public boolean delete(UUID id) {
        if (!petRepository.existsById(id)) {
            return false;
        }
        petRepository.deleteById(id);
        return true;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PetDTOs.PetResponse update(UUID id, PetDTOs.PetRecord dto, String newImageUrl) {
        PetEntity pet = petRepository.findById(id).orElseThrow(PetNotFoundException::new);

        pet.setName(dto.name());
        pet.setType(PetEntity.Type.fromString(dto.type()));
        pet.setGender(PetEntity.Gender.fromString(dto.gender()));
        pet.setCity(dto.city());
        pet.setState(dto.state());
        pet.setAge(dto.age());
        pet.setWeight(dto.weight());
        pet.setRace(dto.race());
        if (newImageUrl != null) {
            pet.setImageUrl(newImageUrl);
        }

        validationUtil.validatePet(pet);

        Map<UUID, UserSummary> owners = userClient.findSummaries(
                pet.getOwnerId() == null ? Set.of() : Set.of(pet.getOwnerId()));
        return petMapper.toDTO(pet, ownerOf(owners, pet.getOwnerId()));
    }

    /** Chamado pelo user-service ao apagar um usuário (substitui o cascade REMOVE do JPA). */
    @Transactional
    public long deleteByOwner(UUID ownerId) {
        return petRepository.deleteByOwnerId(ownerId);
    }

    private Page<PetDTOs.PetResponse> withOwners(Page<PetEntity> pets) {
        Set<UUID> ownerIds = pets.stream()
                .map(PetEntity::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, UserSummary> owners = userClient.findSummaries(ownerIds);
        return pets.map(pet -> petMapper.toDTO(pet, ownerOf(owners, pet.getOwnerId())));
    }

    /** Map.of() (imutável) lança NPE em get(null), então tratamos o pet sem dono explicitamente. */
    private UserSummary ownerOf(Map<UUID, UserSummary> owners, UUID ownerId) {
        return ownerId == null ? null : owners.get(ownerId);
    }
}
