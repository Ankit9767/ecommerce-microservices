package com.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GatewayAuthenticationFilter extends OncePerRequestFilter {

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

        /*
         * InternalServiceAuthenticationFilter runs before this filter.
         */
        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Gateway/user authentication.
         */
        String username = request.getHeader(
                        GatewaySecurityHeaders.AUTHENTICATED_USER
                );

        String userIdHeader =
                request.getHeader(
                        GatewaySecurityHeaders.AUTHENTICATED_USER_ID
                );

        String email = request.getHeader(
                GatewaySecurityHeaders.AUTHENTICATED_USER_EMAIL
        );

        String rolesHeader =
                request.getHeader(
                        GatewaySecurityHeaders.USER_ROLES
                );

        if (username == null ||
                username.isBlank() ||
                userIdHeader == null ||
                userIdHeader.isBlank() ||
                rolesHeader == null ||
                rolesHeader.isBlank()) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        Long userId;

        try {
            userId = Long.valueOf(userIdHeader);
        } catch (NumberFormatException ex) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        List<SimpleGrantedAuthority> authorities =
                Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isBlank())
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        if (authorities.isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        GatewayUserPrincipal principal = new GatewayUserPrincipal(
                        userId,
                        username,
                        email
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}