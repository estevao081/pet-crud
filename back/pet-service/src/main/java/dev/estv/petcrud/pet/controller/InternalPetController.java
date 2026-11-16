package dev.estv.petcrud.pet.controller;

import dev.estv.petcrud.pet.service.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** API service-to-service (não roteada pelo gateway). */
@RestController
@RequestMapping("/internal/pets")
public class InternalPetController {

    private final PetService petService;

    public InternalPetController(PetService petService) {
        this.petService = petService;
    }

    @DeleteMapping("/owner/{ownerId}")
    public ResponseEntity<Void> deleteByOwner(@PathVariable UUID ownerId) {
        petService.deleteByOwner(ownerId);
        return ResponseEntity.noContent().build();
    }
}
