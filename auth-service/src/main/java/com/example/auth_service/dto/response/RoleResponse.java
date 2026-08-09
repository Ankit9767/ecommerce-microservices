package com.example.auth_service.dto.response;

import com.example.auth_service.entity.RoleName;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class RoleResponse {

    private Long id;

    private RoleName roleName;

    private Set<String> permissions;
}