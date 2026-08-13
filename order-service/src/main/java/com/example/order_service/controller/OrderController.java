package com.example.order_service.controller;

import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;
    private final OrderMapper mapper;

    public OrderController(OrderService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) throws JsonProcessingException {

        OrderResponse response = service.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
