package com.example.api_gateway.exception;

import com.ecommerce.common.dto.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange,
            Throwable ex) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                resolveMessage(status),
                exchange.getRequest()
                        .getPath()
                        .value(),
                null
        );

        exchange.getResponse()
                .setStatusCode(status);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;

        try {
            bytes = objectMapper.writeValueAsBytes(
                    errorResponse
            );
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        DataBuffer buffer =
                exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes);

        return exchange.getResponse()
                .writeWith(Mono.just(buffer));
    }

    private HttpStatus resolveStatus(Throwable ex) {

        if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            return HttpStatus.NOT_FOUND;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status) {

        return switch (status) {
            case NOT_FOUND ->
                    "Requested resource was not found";

            case INTERNAL_SERVER_ERROR ->
                    "An unexpected gateway error occurred";

            default ->
                    status.getReasonPhrase();
        };
    }
}
