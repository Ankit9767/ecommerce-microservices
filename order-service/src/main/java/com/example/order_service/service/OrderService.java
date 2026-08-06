package com.example.order_service.service;

import com.ecommerce.common.events.PaymentCompletedEvent;
import com.example.order_service.dto.CreateOrderRequest;
import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.entity.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {

    @Transactional
    OrderResponse createOrder(CreateOrderRequest request) throws JsonProcessingException;

    Order getOrder(Long id);

    List<Order> getAllOrders();

    Order updateOrder(Long id, Order order);

    void deleteOrder(Long id);

    void handlePaymentCompleted(PaymentCompletedEvent event);

}
