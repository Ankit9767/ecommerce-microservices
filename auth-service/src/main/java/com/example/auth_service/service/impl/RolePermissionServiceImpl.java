package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.PermissionResponse;
import com.example.auth_service.entity.Permission;
import com.example.auth_service.entity.Role;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.repository.PermissionRepository;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.service.RolePermissionService;
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