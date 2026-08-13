package com.example.order_service.service;

import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.UpdateOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrder(Long id);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByCustomer(Long customerId);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    OrderResponse cancelOrder(Long id);
}