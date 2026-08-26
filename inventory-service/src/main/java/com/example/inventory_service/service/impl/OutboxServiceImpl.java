package com.example.inventory_service.service.impl;

import com.ecommerce.common.events.OutOfStockEvent;
import com.ecommerce.common.events.StockReleasedEvent;
import com.ecommerce.common.events.StockReservedEvent;
import com.ecommerce.common.events.StockUpdatedEvent;
import com.ecommerce.common.exception.OutboxEventCreationException;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxEvent;
import com.ecommerce.common.kafka.OutboxRepository;
import com.example.inventory_service.service.OutboxService;
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
    public void saveStockReservedEvent(StockReservedEvent event) {

        save(
                EventType.STOCK_RESERVED,
                event,
                event.getProductId()
        );
    }

    @Override
    public void saveStockReleasedEvent(StockReleasedEvent event) {

        save(
                EventType.STOCK_RELEASED,
                event,
                event.getProductId()
        );
    }

    @Override
    public void saveStockUpdatedEvent(StockUpdatedEvent event) {

        save(
                EventType.STOCK_UPDATED,
                event,
                event.getProductId()
        );
    }

    @Override
    public void saveOutOfStockEvent(OutOfStockEvent event) {

        save(
                EventType.OUT_OF_STOCK,
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
                    "Failed to serialize inventory outbox event: "
                            + eventType
                            + " for product "
                            + aggregateId,
                    ex
            );
        }
    }
}