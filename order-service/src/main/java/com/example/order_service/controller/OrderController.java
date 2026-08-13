package com.example.order_service.controller;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.security.CurrentUser;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.UpdateOrderRequest;
import com.example.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    private final CurrentUser currentUser;

    public OrderController(OrderService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                     Authentication authentication) {

        OrderResponse response = service.createOrder(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(service.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id,
                                                  Authentication authentication) {

        return ResponseEntity.ok(service.getOrder(id, authentication));
    }

    @GetMapping("/my")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {

        return ResponseEntity.ok(
                service.getOrdersByCustomer(
                        currentUser.getUserId(authentication)
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {

        return ResponseEntity.ok(service.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {

        return ResponseEntity.ok(service.cancelOrder(id));
    }
}