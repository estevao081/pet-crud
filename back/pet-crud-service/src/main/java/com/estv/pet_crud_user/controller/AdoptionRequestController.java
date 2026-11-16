package com.estv.pet_crud_user.controller;

import com.estv.pet_crud_user.dto.AdoptionRequestDTOs;
import com.estv.pet_crud_user.dto.ApiResponse;
import com.estv.pet_crud_user.service.AdoptionRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AdoptionRequestController {

    private final AdoptionRequestService adoptionRequestService;

    public AdoptionRequestController(AdoptionRequestService adoptionRequestService) {
        this.adoptionRequestService = adoptionRequestService;
    }

    @PostMapping("/pets/{id}/adoption-requests")
    public ResponseEntity<ApiResponse<AdoptionRequestDTOs.AdoptionRequestResponse>> create(@PathVariable("id") UUID petId) {
        AdoptionRequestDTOs.AdoptionRequestResponse created = adoptionRequestService.create(petId);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>(true, created, "Pedido de adoção enviado com sucesso"));
    }

    @GetMapping("/adoption-requests")
    public ResponseEntity<ApiResponse<List<AdoptionRequestDTOs.AdoptionRequestResponse>>> list() {
        List<AdoptionRequestDTOs.AdoptionRequestResponse> requests = adoptionRequestService.listForOwner();
        return ResponseEntity.status(200).body(new ApiResponse<>(true, requests, "Notificações de adoção"));
    }

    @PatchMapping("/adoption-requests/{id}/accept")
    public ResponseEntity<ApiResponse<AdoptionRequestDTOs.AdoptionRequestResponse>> accept(@PathVariable UUID id) {
        AdoptionRequestDTOs.AdoptionRequestResponse response = adoptionRequestService.respond(id, true);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, response, "Pedido de adoção aceito"));
    }

    @PatchMapping("/adoption-requests/{id}/reject")
    public ResponseEntity<ApiResponse<AdoptionRequestDTOs.AdoptionRequestResponse>> reject(@PathVariable UUID id) {
        AdoptionRequestDTOs.AdoptionRequestResponse response = adoptionRequestService.respond(id, false);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, response, "Pedido de adoção recusado"));
    }
}
