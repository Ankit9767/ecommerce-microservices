package com.ecommerce.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@ConditionalOnProperty(
        name = "security.gateway.enabled",
        havingValue = "true")
public class GatewaySecurityConfiguration {

    @Bean
    GatewayAuthenticationFilter gatewayAuthenticationFilter() {
        return new GatewayAuthenticationFilter();
    }

    @Bean
    SecurityFilterChain gatewaySecurityFilterChain(HttpSecurity http,
                                                   GatewayAuthenticationFilter filter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .addFilterBefore(filter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/actuator/**")
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }
}