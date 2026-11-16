package dev.estv.petcrud.user.controller;

import dev.estv.petcrud.user.dto.InternalUserDTOs;
import dev.estv.petcrud.user.entity.UserEntity;
import dev.estv.petcrud.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * API service-to-service. O gateway NÃO roteia /internal/**, então só quem está na rede
 * interna do Docker alcança estes endpoints. Nunca publique a porta deste serviço no host.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/by-email")
    public ResponseEntity<InternalUserDTOs.CredentialsResponse> findByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(u -> ResponseEntity.ok(new InternalUserDTOs.CredentialsResponse(
                        u.getId(), u.getName(), u.getEmail(), u.getPassword(), u.getRole().name())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InternalUserDTOs.CreatedUserResponse> create(
            @RequestBody InternalUserDTOs.CreateUserRequest dto) {
        UserEntity user = userService.create(dto);
        return ResponseEntity.status(201).body(new InternalUserDTOs.CreatedUserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
    }

    /** POST (e não GET com ?ids=) para não estourar o limite de tamanho de URL em páginas grandes. */
    @PostMapping("/summaries")
    public List<InternalUserDTOs.UserSummary> summaries(@RequestBody Set<UUID> ids) {
        return userService.findSummaries(ids);
    }
}
