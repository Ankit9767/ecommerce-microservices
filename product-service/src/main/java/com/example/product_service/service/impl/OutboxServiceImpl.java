package com.example.product_service.service.impl;

import com.ecommerce.common.events.ProductCreatedEvent;
import com.ecommerce.common.events.ProductDeletedEvent;
import com.ecommerce.common.events.ProductUpdatedEvent;
import com.ecommerce.common.exception.OutboxEventCreationException;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxEvent;
import com.ecommerce.common.kafka.OutboxRepository;
import com.example.product_service.service.OutboxService;
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
    public void saveProductCreatedEvent(ProductCreatedEvent event) {

        save(
                EventType.PRODUCT_CREATED,
                event,
                event.getProductId()
        );
    }

    @Override
    public void saveProductUpdatedEvent(ProductUpdatedEvent event) {

        save(
                EventType.PRODUCT_UPDATED,
                event,
                event.getProductId()
        );
    }

    @Override
    public void saveProductDeletedEvent(ProductDeletedEvent event) {

        save(
                EventType.PRODUCT_DELETED,
                event,
                event.getProductId()
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
                    "Failed to serialize product outbox event: "
                            + eventType
                            + " for product "
                            + aggregateId,
                    ex
            );
        }
    }
}