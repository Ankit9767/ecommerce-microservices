package com.example.order_service.service;

import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.UpdateOrderRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, Authentication authentication);

    OrderResponse getOrder(Long id, Authentication authentication);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByCustomer(Long customerId);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    OrderResponse cancelOrder(Long id);
}