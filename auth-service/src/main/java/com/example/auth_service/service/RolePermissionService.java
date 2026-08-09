package com.example.auth_service.service;

import com.example.auth_service.dto.response.PermissionResponse;
import java.util.List;

public interface RolePermissionService {

    List<PermissionResponse> getRolePermissions(Long roleId);

    void assignPermission(Long roleId, Long permissionId);

    void removePermission(Long roleId, Long permissionId);
}