package com.example.auth_service.dto.response;

import com.example.auth_service.entity.PermissionName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {

    private Long id;

    private PermissionName permissionName;
}