package com.example.notification_service.config;

import com.ecommerce.common.security.GatewaySecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


@Configuration
@EnableMethodSecurity
@Import(GatewaySecurityConfiguration.class)
public class SecurityConfig {

}