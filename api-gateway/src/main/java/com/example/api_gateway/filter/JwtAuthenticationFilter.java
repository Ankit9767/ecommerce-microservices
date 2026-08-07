//package com.example.api_gateway.filter;
//
//
//import com.example.api_gateway.service.JwtService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
//
//    private final JwtService jwtService;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange,
//                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
//
//        String path = exchange.getRequest()
//                .getURI()
//                .getPath();
//
//        // Public endpoints
//        if (path.startsWith("/auth") ||
//                path.startsWith("/actuator") ||
//                path.startsWith("/eureka")) {
//
//            return chain.filter(exchange);
//        }
//
//        String authHeader = exchange.getRequest()
//                .getHeaders()
//                .getFirst(HttpHeaders.AUTHORIZATION);
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        String token = authHeader.substring(7);
//
//        if (!jwtService.isTokenValid(token)) {
//
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        String username = jwtService.extractUsername(token);
//        String role = jwtService.extractRole(token);
//
//        ServerWebExchange modifiedExchange =
//                exchange.mutate()
//                        .request(
//                                exchange.getRequest()
//                                        .mutate()
//                                        .header("X-Authenticated-User", username)
//                                        .header("X-User-Role", role)
//                                        .build()
//                        )
//                        .build();
//
//        System.out.println(
//                modifiedExchange.getRequest().getHeaders()
//        );
//
//        return chain.filter(modifiedExchange);
//    }
//
//    @Override
//    public int getOrder() {
//        return -1;
//    }
//}
