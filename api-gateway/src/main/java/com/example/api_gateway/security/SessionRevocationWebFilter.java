package com.example.api_gateway.security;

import com.ecommerce.common.security.JwtConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
public class SessionRevocationWebFilter implements WebFilter {

    private static final String KEY_PREFIX =
            "revoked:session:";

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain chain) {

        return ReactiveSecurityContextHolder
                .getContext()
                .flatMap(context -> {

                    if (context.getAuthentication() == null) {

                        return chain.filter(exchange);
                    }

                    if (!(context.getAuthentication()
                            instanceof JwtAuthenticationToken authentication)) {

                        return chain.filter(exchange);
                    }

                    String sessionId =
                            authentication.getToken()
                                    .getClaimAsString(
                                            JwtConstants.SESSION_ID
                                    );

                    if (sessionId == null ||
                            sessionId.isBlank()) {

                        return unauthorized(exchange);
                    }

                    return redisTemplate
                            .hasKey(
                                    KEY_PREFIX + sessionId
                            )
                            .flatMap(revoked -> {

                                if (Boolean.TRUE.equals(
                                        revoked
                                )) {

                                    return unauthorized(
                                            exchange
                                    );
                                }

                                return chain.filter(
                                        exchange
                                );
                            });
                })
                .switchIfEmpty(
                        chain.filter(exchange)
                );
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(
                        HttpStatus.UNAUTHORIZED
                );

        return exchange.getResponse()
                .setComplete();
    }
}