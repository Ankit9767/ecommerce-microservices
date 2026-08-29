package com.example.api_gateway.filter;

import com.ecommerce.common.security.GatewaySecurityHeaders;
import com.example.api_gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {

            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }

        String username = jwtService.extractUsername(token);

        Long userId = jwtService.extractUserId(token);

        String email = jwtService.extractEmail(token);

        List<String> roles = jwtService.extractRoles(token);

        ServerWebExchange modifiedExchange =
                exchange.mutate()
                        .request(
                                exchange.getRequest()
                                        .mutate()
                                        .headers(headers -> {

                                            // Never trust security headers
                                            // supplied by the client.
                                            headers.remove(
                                                    GatewaySecurityHeaders.AUTHENTICATED_USER
                                            );

                                            headers.remove(
                                                    GatewaySecurityHeaders.AUTHENTICATED_USER_ID
                                            );

                                            headers.remove(
                                                    GatewaySecurityHeaders.USER_ROLES
                                            );

                                            // Recreate them from the
                                            // validated JWT.
                                            headers.set(
                                                    GatewaySecurityHeaders.AUTHENTICATED_USER,
                                                    username
                                            );

                                            headers.set(
                                                    GatewaySecurityHeaders.AUTHENTICATED_USER_ID,
                                                    String.valueOf(userId)
                                            );

                                            if (email != null && !email.isBlank()) {
                                                headers.set(
                                                        GatewaySecurityHeaders.AUTHENTICATED_USER_EMAIL,
                                                        email
                                                );
                                            }

                                            headers.set(
                                                    GatewaySecurityHeaders.USER_ROLES,
                                                    String.join(",", roles)
                                            );
                                        })
                                        .build()
                        )
                        .build();

        return chain.filter(modifiedExchange);
    }

    private boolean isPublicEndpoint(String path) {

        return path.startsWith("/api/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/eureka/");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}