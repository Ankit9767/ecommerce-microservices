package com.example.order_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RoleSecurity {

    public boolean hasRole(Authentication authentication,
                           String role) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return false;
        }

        String requiredRole = normalizeRole(role);

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::normalizeRole)
                .anyMatch(requiredRole::equals);
    }

    public boolean hasAnyRole(Authentication authentication,
                              String... roles) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::normalizeRole)
                .anyMatch(currentRole ->
                        Arrays.stream(roles)
                                .map(this::normalizeRole)
                                .anyMatch(currentRole::equals)
                );
    }

    private String normalizeRole(String role) {

        if (role == null) {
            return null;
        }

        return role.startsWith("ROLE_")
                ? role.substring(5)
                : role;
    }
}