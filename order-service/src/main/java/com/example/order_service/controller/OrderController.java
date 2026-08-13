package com.example.order_service.controller;

import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = service.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(
                service.getAllOrders()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.getOrder(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                service.getOrdersByCustomer(customerId)
        );
    }
}