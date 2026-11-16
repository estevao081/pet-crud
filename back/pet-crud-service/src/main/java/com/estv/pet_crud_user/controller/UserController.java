package com.estv.pet_crud_user.controller;

import com.estv.pet_crud_user.dto.ApiResponse;
import com.estv.pet_crud_user.dto.UserDTOs;
import com.estv.pet_crud_user.entity.UserModel;
import com.estv.pet_crud_user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<UserDTOs.UserResponse>>> findAll() {
        List<UserDTOs.UserResponse> users = userService.findAll();
        return ResponseEntity.status(200).body(new ApiResponse<>(true, users, "Users list"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserModel>> findById(@PathVariable UUID id) {
        UserModel user = userService.findById(id);
        return ResponseEntity.status(200).body(new ApiResponse<>(true, user, "User info"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        boolean deleted = userService.delete(id);
        if (!deleted) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, null, "User not found"));
        }
        return ResponseEntity.status(204).body(
                new ApiResponse<>(true, null, "User removed successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserModel>> update(@PathVariable(value = "id") UUID id,
                                                         @RequestBody @Valid UserDTOs.UserUpdate dto) {
        if (userService.findById(id) == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, null, "User not found"));
        }
        UserModel updatedUser = userService.update(id, dto);
        //updatedUser.setPassword(null);
        return ResponseEntity.status(200)
                .body(new ApiResponse<>(true, updatedUser, "User updated succesfuly"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserModel>> save(@RequestBody @Valid UserDTOs.UserRecord dto) {
        userService.save(dto);
        return ResponseEntity.status(201)
                .body(new ApiResponse<>(true, null, "User created"));
    }
}
