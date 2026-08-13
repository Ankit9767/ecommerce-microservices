package com.example.order_service.service.impl;

import com.ecommerce.common.events.OrderCreatedEvent;
import com.example.order_service.entity.OutboxEvent;
import com.example.order_service.repository.OutboxRepository;
import com.example.order_service.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl
        implements OutboxService {

    private final OutboxRepository repository;

    private final ObjectMapper objectMapper;

    @Override
    public void saveOrderCreatedEvent(OrderCreatedEvent event)
            throws JsonProcessingException {

        OutboxEvent outbox = OutboxEvent.builder()
                .eventType("ORDER_CREATED")
                .aggregateId(event.getOrderId())
                .payload(objectMapper.writeValueAsString(event))
                .published(false)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(outbox);

    }
}
