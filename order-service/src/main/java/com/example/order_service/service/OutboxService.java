package com.example.order_service.service;

import com.ecommerce.common.events.OrderCancelledEvent;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface OutboxService {

    void saveOrderCreatedEvent(OrderCreatedEvent event)
            throws JsonProcessingException;

    void saveOrderCancelledEvent(OrderCancelledEvent event)
            throws JsonProcessingException;

}