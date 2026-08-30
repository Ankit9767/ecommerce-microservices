package com.example.order_service.service;

import com.ecommerce.common.events.OrderCancelledEvent;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.OrderPaidEvent;

public interface OutboxService {

    void saveOrderCreatedEvent(OrderCreatedEvent event);

    void saveOrderCancelledEvent(OrderCancelledEvent event);

    void saveOrderPaidEvent(OrderPaidEvent event);
}