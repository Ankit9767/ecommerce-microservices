package com.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader(
                        GatewaySecurityHeaders.AUTHENTICATED_USER
                );

        String rolesHeader =
                request.getHeader(
                        GatewaySecurityHeaders.USER_ROLES
                );

        System.out.println("========== COMMON GATEWAY AUTH ==========");
        System.out.println("URI = " + request.getRequestURI());
        System.out.println(
                "X-Authenticated-User = [" +
                        username +
                        "]"
        );
        System.out.println(
                "X-User-Roles = [" +
                        rolesHeader +
                        "]"
        );
        System.out.println("=========================================");

        if (username == null ||
                username.isBlank() ||
                rolesHeader == null ||
                rolesHeader.isBlank()) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

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

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}