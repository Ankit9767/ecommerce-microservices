package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.PermissionResponse;
import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.Permission;
import com.example.auth_service.entity.Role;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.repository.PermissionRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.service.RolePermissionService;
import com.example.auth_service.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final SecurityAuditService securityAuditService;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getRolePermissions(Long roleId) {

        Role role = findRole(roleId);

        return role.getPermissions()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void assignPermission(Long roleId, Long permissionId) {

        Role role = findRole(roleId);

        Permission permission = findPermission(permissionId);

        if (role.getPermissions().contains(permission)) {

            throw new IllegalStateException(
                    "Permission already assigned to role: "
                            + permission.getPermissionName()
            );
        }

        role.getPermissions().add(permission);

        roleRepository.save(role);

        securityAuditService.record(
                AuditEventType.PERMISSION_ASSIGNED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "Permission "
                        + permission.getPermissionName()
                        + " assigned to role "
                        + role.getRoleName()
        );
    }

    @Override
    public void removePermission(Long roleId, Long permissionId) {

        Role role = findRole(roleId);

        Permission permission = findPermission(permissionId);

        if (!role.getPermissions().contains(permission)) {

            throw new IllegalStateException(
                    "Permission is not assigned to role: "
                            + permission.getPermissionName()
            );
        }

        role.getPermissions().remove(permission);

        roleRepository.save(role);

        securityAuditService.record(
                AuditEventType.PERMISSION_REMOVED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "Permission "
                        + permission.getPermissionName()
                        + " removed from role "
                        + role.getRoleName()
        );
    }

    private Role findRole(Long roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found: " + roleId
                        )
                );
    }

    private Permission findPermission(Long permissionId) {

        return permissionRepository.findById(permissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Permission not found: "
                                        + permissionId
                        )
                );
    }

    private PermissionResponse mapToResponse(Permission permission) {

        return PermissionResponse.builder()
                .id(permission.getId())
                .permissionName(
                        permission.getPermissionName()
                )
                .build();
    }
}