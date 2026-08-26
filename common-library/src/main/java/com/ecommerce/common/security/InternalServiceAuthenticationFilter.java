package com.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private final String internalServiceToken;

    public InternalServiceAuthenticationFilter(String internalServiceToken) {

        this.internalServiceToken = internalServiceToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return request.getRequestURI()
                .startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String providedToken = request.getHeader("X-Service-Token");

        /*
         * No service token means this may be a normal
         * Gateway/user request.
         */

        if (providedToken == null || providedToken.isBlank()) {

            filterChain.doFilter(request, response);
            return;
        }

        if (internalServiceToken == null ||
                internalServiceToken.isBlank()) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        if (!constantTimeEquals(
                providedToken,
                internalServiceToken)) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        /*
         * Authenticate the calling service.
         */
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_INTERNAL_SERVICE"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean constantTimeEquals(String provided,
                                       String expected) {

        if (provided.length() != expected.length()) {
            return false;
        }

        int result = 0;

        for (int i = 0; i < provided.length(); i++) {
            result |= provided.charAt(i)
                    ^ expected.charAt(i);
        }

        return result == 0;
    }
}