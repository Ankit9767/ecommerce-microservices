package com.example.api_gateway.security;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver ipKeyResolver() {

        return exchange -> {

            String ipAddress =
                    exchange.getRequest()
                            .getRemoteAddress()
                            .getAddress()
                            .getHostAddress();

            return Mono.just(ipAddress);
        };
    }
}