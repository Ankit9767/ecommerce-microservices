package com.example.auth_service.controller;

import com.example.auth_service.dto.response.PermissionResponse;
import com.example.auth_service.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<List<PermissionResponse>>
    getRolePermissions(@PathVariable Long roleId) {

        return ResponseEntity.ok(rolePermissionService.getRolePermissions(roleId));
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> assignPermission(@PathVariable Long roleId,
            @PathVariable Long permissionId) {

        rolePermissionService.assignPermission(roleId, permissionId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> removePermission(@PathVariable Long roleId,
            @PathVariable Long permissionId) {

        rolePermissionService.removePermission(roleId, permissionId);

        return ResponseEntity.noContent().build();
    }
}