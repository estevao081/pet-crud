package dev.estv.petcrud.pet.controller;

import dev.estv.petcrud.pet.dto.ApiResponse;
import dev.estv.petcrud.pet.dto.PetDTOs;
import dev.estv.petcrud.pet.security.AuthenticatedUser;
import dev.estv.petcrud.pet.service.PetService;
import dev.estv.petcrud.pet.util.ReturnImageURL;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;
    private final ReturnImageURL returnImageURL;

    public PetController(PetService petService, ReturnImageURL returnImageURL) {
        this.petService = petService;
        this.returnImageURL = returnImageURL;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> save(@ModelAttribute @Valid PetDTOs.PetRecord dto,
                                                  @RequestParam("image") MultipartFile image,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        String imageUrl = returnImageURL.imageUrl(image);
        petService.save(dto, imageUrl, user.id());
        return ResponseEntity.status(201).body(new ApiResponse<>(true, null, "Pet created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PetDTOs.PetResponse>>> findAll(@RequestParam int page,
                                                                         @RequestParam int items) {
        return ResponseEntity.ok(new ApiResponse<>(true, petService.listPets(page, items), "Pet list"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        if (!petService.delete(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, null, "Pet not found"));
        }
        return ResponseEntity.status(204).body(new ApiResponse<>(true, null, "Pet removed successfully"));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<PetDTOs.PetResponse>>> search(@RequestBody PetDTOs.PetResponse filter,
                                                                        @RequestParam int page,
                                                                        @RequestParam int items) {
        return ResponseEntity.ok(new ApiResponse<>(true, petService.search(filter, page, items), "Search result"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetDTOs.PetResponse>> update(
            @PathVariable UUID id,
            @ModelAttribute @Valid PetDTOs.PetRecord dto,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        String imageUrl = (image != null && !image.isEmpty()) ? returnImageURL.imageUrl(image) : null;
        return ResponseEntity.ok(new ApiResponse<>(true, petService.update(id, dto, imageUrl), "Pet updated successfully"));
    }
}
