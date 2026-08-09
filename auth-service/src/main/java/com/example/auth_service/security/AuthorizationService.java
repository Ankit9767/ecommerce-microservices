package com.example.auth_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authorizationService")
public class AuthorizationService {

    public boolean isCurrentUser(String username,
            Authentication authentication) {

        if (username == null || authentication == null) {
            return false;
        }

        return username.equals(authentication.getName());
    }

    public boolean isAdmin(Authentication authentication) {

        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority
                                .getAuthority()
                                .equals("ROLE_ADMIN")
                );
    }
}