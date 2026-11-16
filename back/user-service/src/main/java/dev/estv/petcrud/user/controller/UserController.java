package dev.estv.petcrud.user.controller;

import dev.estv.petcrud.user.dto.ApiResponse;
import dev.estv.petcrud.user.dto.UserDTOs;
import dev.estv.petcrud.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** API pública (via gateway). Mesmos endpoints e formatos do monólito. */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<UserDTOs.UserResponse>>> findAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, userService.findAll(), "Users list"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        if (!userService.delete(id)) {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, null, "User not found"));
        }
        return ResponseEntity.status(204).body(new ApiResponse<>(true, null, "User removed successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTOs.UserResponse>> update(@PathVariable UUID id,
                                                                    @RequestBody @Valid UserDTOs.UserUpdate dto) {
        // User not found -> UserNotFoundException -> 404 (GlobalExceptionHandler)
        return ResponseEntity.ok(new ApiResponse<>(true, userService.update(id, dto), "User updated succesfuly"));
    }
}
