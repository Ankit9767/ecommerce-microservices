package com.example.order_service.service;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.UpdateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, Authentication authentication);

    OrderResponse getOrder(Long id, Authentication authentication);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    Page<OrderResponse> getOrdersByCustomer(Long customerId, Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, Authentication authentication, Pageable pageable);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    OrderResponse cancelOrder(Long id, Authentication authentication);

    OrderResponse createOrderFromCart(Authentication authentication);
}