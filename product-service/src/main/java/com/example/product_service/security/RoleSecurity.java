package com.example.product_service.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSecurity {

    private final HttpServletRequest request;

    public boolean hasRole(String role) {

        String currentRole =
                request.getHeader("X-User-Role");

        return role.equals(currentRole);
    }

    public boolean hasAnyRole(String... roles) {

        String currentRole = request.getHeader("X-User-Role");

        System.out.println("Current Role = " + currentRole);

        for (String role : roles) {
            System.out.println("Checking against = " + role);

            if (role.equals(currentRole)) {
                return true;
            }
        }

        return false;
    }
}
