package com.example.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID = "X-Request-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String requestId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(REQUEST_ID);

        if (requestId == null ||
                requestId.isBlank()) {

            requestId = UUID.randomUUID().toString();
        }

        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .header(
                                REQUEST_ID,
                                requestId
                        )
                        .build();

        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(request)
                        .build();

        String finalRequestId = requestId;

        mutatedExchange.getResponse()
                .getHeaders()
                .add(
                        REQUEST_ID,
                        finalRequestId
                );

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}