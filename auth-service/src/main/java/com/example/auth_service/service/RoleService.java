package com.example.auth_service.service;

import com.example.auth_service.dto.response.RoleResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface RoleService {

    @PreAuthorize("hasRole('ADMIN')")
    List<RoleResponse> getAllRoles();

    @PreAuthorize("hasRole('ADMIN')")
    RoleResponse getRole(Long roleId);
}