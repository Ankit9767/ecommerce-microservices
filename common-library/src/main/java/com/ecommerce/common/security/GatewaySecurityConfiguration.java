package com.ecommerce.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

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
    InternalServiceAuthenticationFilter internalServiceAuthenticationFilter(
            @Value("${service.security.internal-token}")
            String internalServiceToken) {

        return new InternalServiceAuthenticationFilter(internalServiceToken);
    }

    @Bean
    SecurityFilterChain gatewaySecurityFilterChain(
            HttpSecurity http,
            GatewayAuthenticationFilter gatewayFilter,
            InternalServiceAuthenticationFilter internalFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                /*
                 * First check whether this is an internal
                 * service-to-service request.
                 */
                .addFilterAfter(
                        internalFilter,
                        SecurityContextHolderFilter.class
                )

                /*
                 * Then check Gateway/user authentication.
                 */
                .addFilterAfter(
                        gatewayFilter,
                        InternalServiceAuthenticationFilter.class
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