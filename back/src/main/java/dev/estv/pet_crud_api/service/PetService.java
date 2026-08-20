package dev.estv.pet_crud_api.service;

import dev.estv.pet_crud_api.dto.PetDTOs;
import dev.estv.pet_crud_api.entity.PetModel;
import dev.estv.pet_crud_api.entity.UserModel;
import dev.estv.pet_crud_api.repository.PetRepository;
import dev.estv.pet_crud_api.repository.UserRepository;
import dev.estv.pet_crud_api.specification.PetSpecification;
import dev.estv.pet_crud_api.util.PetMapper;
import dev.estv.pet_crud_api.util.ValidationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PetService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final ValidationUtil validationUtil;

    public PetService(UserRepository userRepository, PetRepository petRepository, PetMapper petMapper,
            ValidationUtil validationUtil) {
        this.userRepository = userRepository;
        this.petRepository = petRepository;
        this.petMapper = petMapper;
        this.validationUtil = validationUtil;
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
        String usermail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserModel user = userRepository.findByUsermail(usermail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PetModel pet = petMapper.toEntity(dto, user);

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
