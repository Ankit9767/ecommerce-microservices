package com.example.auth_service.controller;

import com.example.auth_service.dto.request.AccountStatusRequest;
import com.example.auth_service.dto.request.ChangePasswordRequest;
import com.example.auth_service.dto.request.UpdateProfileRequest;
import com.example.auth_service.dto.response.RoleResponse;
import com.example.auth_service.dto.response.UserProfileResponse;
import com.example.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {

        return ResponseEntity.ok(
                userService.getCurrentUser()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userService.updateCurrentUser(request)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> updateAccountStatus(@PathVariable Long userId,
            @Valid @RequestBody AccountStatusRequest request) {

        userService.updateAccountStatus(userId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<List<RoleResponse>> getUserRoles(
            @PathVariable Long userId) {

        return ResponseEntity.ok(userService.getUserRoles(userId));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> assignRole(@PathVariable Long userId,
            @PathVariable Long roleId) {

        userService.assignRole(userId, roleId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRole(@PathVariable Long userId,
            @PathVariable Long roleId) {

        userService.removeRole(userId, roleId);

        return ResponseEntity.noContent().build();
    }
}