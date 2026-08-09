package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.RoleResponse;
import com.example.auth_service.entity.Role;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RoleResponse getRole(Long roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found: " + roleId
                        )
                );

        return mapToResponse(role);
    }

    private RoleResponse mapToResponse(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .permissions(
                        role.getPermissions()
                                .stream()
                                .map(permission ->
                                        permission
                                                .getPermissionName()
                                                .name()
                                )
                                .collect(
                                        java.util.stream.Collectors.toSet()
                                )
                )
                .build();
    }
}