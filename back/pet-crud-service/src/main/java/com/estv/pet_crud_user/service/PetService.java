package com.estv.pet_crud_user.service;

import com.estv.pet_crud_user.dto.PetDTOs;
import com.estv.pet_crud_user.entity.PetModel;
import com.estv.pet_crud_user.entity.UserModel;
import com.estv.pet_crud_user.repository.PetRepository;
import com.estv.pet_crud_user.specification.PetSpecification;
import com.estv.pet_crud_user.util.PetMapper;
import com.estv.pet_crud_user.util.ValidationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final ValidationUtil validationUtil;
    private final UserService userService;

    public PetService(PetRepository petRepository, PetMapper petMapper,
                      ValidationUtil validationUtil, UserService userService) {
        this.petRepository = petRepository;
        this.petMapper = petMapper;
        this.validationUtil = validationUtil;
        this.userService = userService;
    }

    public Page<PetDTOs.PetResponse> listPets(int page, int items) {
        Pageable pageable = PageRequest.of(page, items, Sort.by("createdAt").descending());
        return petRepository.findAll(pageable).map(petMapper::toDTO);
    }

    public Page<PetDTOs.PetResponse> search(PetDTOs.PetResponse filter, int page, int items) {
        Pageable pageable = PageRequest.of(page, items, Sort.by("createdAt").descending());
        return petRepository.findAll(PetSpecification.filter(filter), pageable).map(petMapper::toDTO);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PetModel save(PetDTOs.PetRecord dto, String imageUrl) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UUID userId = UUID.fromString(authentication.getName());

        UserModel user = userService.findById(userId);

        PetModel pet = petMapper.toEntity(dto, user);

        pet.setId(userId);
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
    public PetModel update(UUID id, PetDTOs.PetRecord dto, String newImageUrl) {
        PetModel petModel = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        String oldImage = petModel.getImageUrl();

        BeanUtils.copyProperties(dto, petModel);

        petModel.setImageUrl(
                newImageUrl != null ? newImageUrl : oldImage);

        validationUtil.validatePet(petModel);

        return petModel;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PetModel findById(UUID id) {
        return petRepository.findById(id).orElse(null);
    }
}
