package dev.estv.petcrud.pet.repository;

import dev.estv.petcrud.pet.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<PetEntity, UUID>, JpaSpecificationExecutor<PetEntity> {

    long deleteByOwnerId(UUID ownerId);
}
