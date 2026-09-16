package com.example.api_gateway.controller;

import com.ecommerce.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class FallbackController {

    @GetMapping("/fallback/auth")
    public ResponseEntity<ErrorResponse> authFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Auth service is currently unavailable"
        );
    }

    @GetMapping("/fallback/cart")
    public ResponseEntity<ErrorResponse> cartFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Cart service is currently unavailable"
        );
    }

    @GetMapping("/fallback/products")
    public ResponseEntity<ErrorResponse> productFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Product service is currently unavailable"
        );
    }

    @GetMapping("/fallback/orders")
    public ResponseEntity<ErrorResponse> orderFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Order service is currently unavailable"
        );
    }

    @GetMapping("/fallback/inventory")
    public ResponseEntity<ErrorResponse> inventoryFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Inventory service is currently unavailable"
        );
    }

    @GetMapping("/fallback/payments")
    public ResponseEntity<ErrorResponse> paymentFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Payment service is currently unavailable"
        );
    }

    @GetMapping("/fallback/notifications")
    public ResponseEntity<ErrorResponse> notificationFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Notification service is currently unavailable"
        );
    }

    @GetMapping("/fallback/shipments")
    public ResponseEntity<ErrorResponse> shipmentFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Shipment service is currently unavailable"
        );
    }

    @GetMapping("/fallback/search")
    public ResponseEntity<ErrorResponse> searchFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Search service is currently unavailable"
        );
    }

    @GetMapping("/fallback/reviews")
    public ResponseEntity<ErrorResponse> reviewsFallback(ServerWebExchange exchange) {
        return fallback(exchange,
                "Reviews service is currently unavailable"
        );
    }

    private ResponseEntity<ErrorResponse> fallback(ServerWebExchange exchange,
                                                   String message) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}