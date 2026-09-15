package dev.estv.pet_crud_api.service;

import dev.estv.pet_crud_api.dto.AdoptionRequestDTOs;
import dev.estv.pet_crud_api.entity.AdoptionRequestModel;
import dev.estv.pet_crud_api.entity.PetModel;
import dev.estv.pet_crud_api.entity.UserModel;
import dev.estv.pet_crud_api.repository.AdoptionRequestRepository;
import dev.estv.pet_crud_api.repository.PetRepository;
import dev.estv.pet_crud_api.repository.UserRepository;
import dev.estv.pet_crud_api.util.AdoptionRequestMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdoptionRequestService {

    private static final String NOT_INFORMED = "não informado";

    private final AdoptionRequestRepository adoptionRequestRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AdoptionRequestMapper adoptionRequestMapper;

    public AdoptionRequestService(AdoptionRequestRepository adoptionRequestRepository,
                                   PetRepository petRepository,
                                   UserRepository userRepository,
                                   AdoptionRequestMapper adoptionRequestMapper) {
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.adoptionRequestMapper = adoptionRequestMapper;
    }

    private UserModel currentUser() {
        String usermail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsermail(usermail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public AdoptionRequestDTOs.AdoptionRequestResponse create(UUID petId) {
        UserModel requester = currentUser();

        PetModel pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet não encontrado"));

        if (pet.getOwner() == null) {
            throw new IllegalArgumentException("Este pet não possui um tutor responsável");
        }

        if (pet.getOwner().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("Você não pode solicitar a adoção do seu próprio pet");
        }

        if (adoptionRequestRepository.existsByPetIdAndRequesterIdAndStatus(
                petId, requester.getId(), AdoptionRequestModel.Status.PENDING)) {
            throw new IllegalArgumentException("Você já solicitou a adoção deste pet");
        }

        AdoptionRequestModel request = new AdoptionRequestModel();
        request.setPet(pet);
        request.setOwner(pet.getOwner());
        request.setRequester(requester);
        request.setRequesterName(nonBlankOr(requester.getName()));
        request.setRequesterPhone(nonBlankOr(requester.getNumber()));
        request.setRequesterAddress(nonBlankOr(requester.getAddress()));
        request.setStatus(AdoptionRequestModel.Status.PENDING);

        return adoptionRequestMapper.toDTO(adoptionRequestRepository.save(request));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<AdoptionRequestDTOs.AdoptionRequestResponse> listForOwner() {
        UserModel owner = currentUser();

        return adoptionRequestRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(adoptionRequestMapper::toDTO)
                .toList();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public AdoptionRequestDTOs.AdoptionRequestResponse respond(UUID requestId, boolean accept) {
        UserModel user = currentUser();

        AdoptionRequestModel request = adoptionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));

        if (!request.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para responder esta solicitação");
        }

        if (request.getStatus() != AdoptionRequestModel.Status.PENDING) {
            throw new IllegalArgumentException("Esta solicitação já foi respondida");
        }

        request.setStatus(accept ? AdoptionRequestModel.Status.ACCEPTED : AdoptionRequestModel.Status.REJECTED);
        request.setRespondedAt(LocalDateTime.now());

        return adoptionRequestMapper.toDTO(adoptionRequestRepository.save(request));
    }

    private String nonBlankOr(String value) {
        return (value == null || value.isBlank()) ? NOT_INFORMED : value;
    }
}
