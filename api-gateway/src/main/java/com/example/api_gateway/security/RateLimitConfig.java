package com.example.api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Value("${gateway.rate-limit.use-forwarded-for:false}")
    private boolean useForwardedFor;

    @Bean
    public KeyResolver ipKeyResolver() {

        return exchange -> {

            String ipAddress;

            if (useForwardedFor) {

                String forwardedFor =
                        exchange.getRequest()
                                .getHeaders()
                                .getFirst("X-Forwarded-For");

                if (forwardedFor != null && !forwardedFor.isBlank()) {

                    ipAddress =
                            forwardedFor
                                    .split(",")[0]
                                    .trim();

                } else {
                    ipAddress = getRemoteAddress(exchange);
                }

            } else {

                ipAddress = getRemoteAddress(exchange);
            }

            return Mono.just(ipAddress);
        };
    }

    private String getRemoteAddress(
            org.springframework.web.server.ServerWebExchange exchange) {

        if (exchange.getRequest()
                .getRemoteAddress() == null) {

            return "unknown";
        }

        return exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();
    }
}