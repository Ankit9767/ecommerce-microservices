package com.example.auth_service.config;

import com.example.auth_service.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final DaoAuthenticationProvider authenticationProvider;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/users/*/status"
                        )
                        .hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/*"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Permission-based authorization
                         */
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/products/**"
                        ).hasAuthority("PRODUCT_READ")


                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/products/**"
                        ).hasAuthority("PRODUCT_CREATE")


                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/products/**"
                        ).hasAuthority("PRODUCT_UPDATE")


                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/api/products/**"
                        ).hasAuthority("PRODUCT_DELETE")


                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/orders/**"
                        ).hasAuthority("ORDER_CREATE")


                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/orders/**"
                        ).hasAuthority("ORDER_READ")


                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/payments/**"
                        ).hasAuthority("PAYMENT_CREATE")

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/api/seller/**"
                        ).hasRole("SELLER")
                        .requestMatchers(
                                "/api/customer/**"
                        ).hasRole("CUSTOMER")
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }

}