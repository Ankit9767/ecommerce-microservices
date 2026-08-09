package com.example.auth_service.service;

import com.example.auth_service.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse getRole(Long roleId);
}