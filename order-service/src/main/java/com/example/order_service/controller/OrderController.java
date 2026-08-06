package com.example.order_service.controller;

import com.example.order_service.dto.CreateOrderRequest;
import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;
    private final OrderMapper mapper;

    public OrderController(OrderService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        List<OrderResponse> responses = service.getAllOrders()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {

        Order order = service.getOrder(id);

        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) throws JsonProcessingException {

        OrderResponse response = service.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @RequestBody Order order) {

        return ResponseEntity.ok(
                service.updateOrder(id, order));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id) {

        service.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }
}
