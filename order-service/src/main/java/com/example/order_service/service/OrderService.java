package com.example.order_service.service;

import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.dto.CreateOrderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {

    @Transactional
    OrderResponse createOrder(CreateOrderRequest request) throws JsonProcessingException;

}
