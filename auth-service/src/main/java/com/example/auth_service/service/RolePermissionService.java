package com.example.auth_service.service;

import com.example.auth_service.dto.response.PermissionResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface RolePermissionService {

    @PreAuthorize("hasRole('ADMIN')")
    List<PermissionResponse> getRolePermissions(Long roleId);

    @PreAuthorize("hasRole('ADMIN')")
    void assignPermission(Long roleId, Long permissionId);

    @PreAuthorize("hasRole('ADMIN')")
    void removePermission(Long roleId, Long permissionId);
}