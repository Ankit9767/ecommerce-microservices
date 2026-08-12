package com.example.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ResponseHeaderSanitizationFilter implements GlobalFilter, Ordered {

    private static final String SERVER = "Server";

    private static final String X_POWERED_BY = "X-Powered-By";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {

                    exchange.getResponse()
                            .getHeaders()
                            .remove(SERVER);

                    exchange.getResponse()
                            .getHeaders()
                            .remove(X_POWERED_BY);
                }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}