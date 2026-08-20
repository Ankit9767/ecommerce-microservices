package com.example.order_service.service;

import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.events.PaymentEvent;
import com.example.order_service.dto.CreateOrderFromCartRequest;
import com.example.order_service.dto.CreateOrderRequest;
import com.example.order_service.dto.UpdateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, Authentication authentication);

    OrderResponse getOrder(Long id, Authentication authentication);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    Page<OrderResponse> getOrdersByCustomer(Long customerId, Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, Authentication authentication, Pageable pageable);

    OrderResponse updateOrder(Long id, UpdateOrderRequest request);

    OrderResponse cancelOrder(Long id, Authentication authentication);

    OrderResponse createOrderFromCart(CreateOrderFromCartRequest request,
                                      Authentication authentication);

    OrderResponse handlePaymentCompleted(PaymentEvent event);
}