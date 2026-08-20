package dev.estv.pet_crud_api.repository;

import dev.estv.pet_crud_api.entity.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
    @Query("SELECT c FROM UserModel c WHERE c.email = :usermail")
    Optional<UserModel> findByUsermail(String usermail);
}
