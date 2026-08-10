package com.example.api_gateway.config;

import com.example.api_gateway.exception.GatewaySecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final GatewaySecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(GatewaySecurityExceptionHandler securityExceptionHandler) {
        this.securityExceptionHandler = securityExceptionHandler;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling

                                .authenticationEntryPoint(
                                        securityExceptionHandler
                                )

                                .accessDeniedHandler(
                                        securityExceptionHandler
                                )
                )

                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        )
                        .permitAll()

                        .pathMatchers("/actuator/health")
                        .permitAll()

                        .anyExchange()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> {})
                )

                .build();
    }
}