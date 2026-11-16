package com.estv.pet_crud_user.repository;

import com.estv.pet_crud_user.entity.AdoptionRequestModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequestModel, UUID> {

    List<AdoptionRequestModel> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    boolean existsByPetIdAndRequesterIdAndStatus(UUID petId, UUID requesterId, AdoptionRequestModel.Status status);
}
