package com.example.cart_service.service.impl;

import com.ecommerce.common.events.CartAbandonedEvent;
import com.ecommerce.common.events.CartCheckedOutEvent;
import com.ecommerce.common.exception.OutboxEventCreationException;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxEvent;
import com.ecommerce.common.kafka.OutboxRepository;
import com.example.cart_service.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxRepository repository;

    private final ObjectMapper objectMapper;

    @Override
    public void saveCartCheckedOutEvent(CartCheckedOutEvent event) {

        save(
                EventType.CART_CHECKED_OUT,
                event,
                event.getCustomerId()
        );
    }

    @Override
    public void saveCartAbandonedEvent(CartAbandonedEvent event) {

        save(
                EventType.CART_ABANDONED,
                event,
                event.getCustomerId()
        );
    }

    private void save(EventType eventType,
                      Object event,
                      Long aggregateId) {

        try {

            OutboxEvent outbox =
                    OutboxEvent.builder()
                            .eventType(eventType.getValue())
                            .aggregateId(aggregateId)
                            .payload(
                                    objectMapper.writeValueAsString(event)
                            )
                            .published(false)
                            .createdAt(LocalDateTime.now())
                            .build();

            repository.save(outbox);

        } catch (JsonProcessingException ex) {

            throw new OutboxEventCreationException(
                    "Failed to serialize cart outbox event: "
                            + eventType
                            + " for customer "
                            + aggregateId,
                    ex
            );
        }
    }
}