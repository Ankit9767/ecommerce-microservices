package com.ecommerce.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Long getUserId(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "No authenticated user"
            );
        }

        GatewayUserPrincipal principal =
                (GatewayUserPrincipal)
                        authentication.getPrincipal();

        return principal.userId();
    }

    public String getUsername(Authentication authentication) {

        GatewayUserPrincipal principal =
                (GatewayUserPrincipal)
                        authentication.getPrincipal();

        return principal.username();
    }
}