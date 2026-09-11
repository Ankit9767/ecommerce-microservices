package com.example.auth_service.repository;

import com.example.auth_service.entity.Permission;
import com.example.auth_service.entity.PermissionName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionName(
            PermissionName permissionName
    );
}