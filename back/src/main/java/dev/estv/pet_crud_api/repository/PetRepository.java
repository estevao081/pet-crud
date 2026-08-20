package dev.estv.pet_crud_api.repository;

import dev.estv.pet_crud_api.entity.PetModel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends
        JpaRepository<PetModel, UUID>,
        JpaSpecificationExecutor<PetModel> {
}
