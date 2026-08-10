package com.example.api_gateway.exception;

import com.ecommerce.common.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GatewaySecurityExceptionHandler implements ServerAuthenticationEntryPoint,
        ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange,
            AuthenticationException exception) {

        return writeError(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Authentication is required"
        );
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange,
            org.springframework.security.access.AccessDeniedException exception) {

        return writeError(
                exchange,
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource"
        );
    }

    private Mono<Void> writeError(ServerWebExchange exchange,
            HttpStatus status,
            String message) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.empty();
        }

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest()
                        .getPath()
                        .value(),
                null
        );

        byte[] bytes;

        try {
            bytes = objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        exchange.getResponse()
                .setStatusCode(status);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse()
                .writeWith(
                        Mono.just(
                                exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(bytes)
                        )
                );
    }
}
