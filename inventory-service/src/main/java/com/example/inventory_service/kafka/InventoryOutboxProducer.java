package com.example.inventory_service.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.events.InventoryEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class InventoryOutboxProducer implements OutboxProducer {

    private final StockEventProducer producer;

    @Override
    public CompletableFuture<?> publish(DomainEvent event) {
        return producer.publish((InventoryEvent) event);
    }

    @Override
    public boolean supports(EventType eventType) {
        return eventType == EventType.STOCK_RESERVED
                || eventType == EventType.STOCK_RELEASED
                || eventType == EventType.STOCK_UPDATED
                || eventType == EventType.OUT_OF_STOCK;
    }
}
