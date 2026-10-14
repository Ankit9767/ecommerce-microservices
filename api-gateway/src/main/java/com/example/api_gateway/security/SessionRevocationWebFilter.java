package com.example.api_gateway.security;

import com.ecommerce.common.security.JwtConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
public class SessionRevocationWebFilter implements WebFilter {

    private static final String KEY_PREFIX = "revoked:session:";

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain chain) {

        return ReactiveSecurityContextHolder.getContext()
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(contextOptional -> {
                    if (contextOptional.isEmpty()) {
                        return chain.filter(exchange);
                    }

                    SecurityContext context = contextOptional.get();

                    if (!(context.getAuthentication()
                            instanceof JwtAuthenticationToken authentication)) {
                        return chain.filter(exchange);
                    }

                    String sessionId = authentication.getToken()
                            .getClaimAsString(JwtConstants.SESSION_ID);

                    if (sessionId == null || sessionId.isBlank()) {
                        return unauthorized(exchange);
                    }

                    // Handle Redis errors separately from downstream chain errors.
                    return redisTemplate.hasKey(KEY_PREFIX + sessionId)
                            .defaultIfEmpty(false)
                            .map(revoked -> revoked
                                    ? CheckResult.REVOKED
                                    : CheckResult.ALLOWED)
                            .onErrorResume(error ->
                                    Mono.just(CheckResult.REDIS_UNAVAILABLE))
                            .flatMap(result -> switch (result) {
                                case REVOKED -> unauthorized(exchange);
                                case REDIS_UNAVAILABLE ->
                                        redisUnavailable(exchange);
                                case ALLOWED -> chain.filter(exchange);
                            });
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> redisUnavailable(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return exchange.getResponse().setComplete();
    }

    private enum CheckResult {
        REVOKED,
        ALLOWED,
        REDIS_UNAVAILABLE
    }
}