package com.example.order_service.service.impl;

import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.metrics.OrderMetrics;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrderRepository repository;

    private final OrderMapper mapper;

    private final OrderMetrics orderMetrics;

    public OrderResponse createOrder(Order order) {

        Order savedOrder = repository.save(order);

        orderMetrics.orderCreated();

        return mapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrder(Order order) {

        Order savedOrder = repository.save(order);

        orderMetrics.orderUpdated();

        return mapper.toResponse(savedOrder);
    }


    public OrderResponse createOrderFromCart(Order order) {

        Order savedOrder = repository.save(order);

        orderMetrics.orderCreated();

        return mapper.toResponse(savedOrder);
    }
}