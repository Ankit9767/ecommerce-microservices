package com.example.auth_service.controller;

import com.example.auth_service.dto.response.RoleResponse;
import com.example.auth_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {

        return ResponseEntity.ok(
                roleService.getAllRoles()
        );
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable Long roleId
    ) {

        return ResponseEntity.ok(
                roleService.getRole(roleId)
        );
    }
}