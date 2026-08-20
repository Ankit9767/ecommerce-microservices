package com.example.inventory_service.kafka;

import com.ecommerce.common.dto.InventoryResponse;
import com.ecommerce.common.events.InventoryEvent;
import com.ecommerce.common.events.OutOfStockEvent;
import com.ecommerce.common.events.StockReleasedEvent;
import com.ecommerce.common.events.StockReservedEvent;
import com.ecommerce.common.events.StockUpdatedEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventProducer {

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    private final MeterRegistry meterRegistry;

    public void stockReserved(InventoryResponse response) {

        publish(StockReservedEvent.builder()
                .eventType(EventType.STOCK_RESERVED)
                .productId(response.productId())
                .quantity(response.quantity())
                .availableQuantity(response.availableQuantity())
                .reservedQuantity(response.reservedQuantity())
                .build());
    }

    public void stockReleased(InventoryResponse response) {

        publish(StockReleasedEvent.builder()
                .eventType(EventType.STOCK_RELEASED)
                .productId(response.productId())
                .quantity(response.quantity())
                .availableQuantity(response.availableQuantity())
                .reservedQuantity(response.reservedQuantity())
                .build());
    }

    public void stockUpdated(InventoryResponse response) {

        publish(StockUpdatedEvent.builder()
                .eventType(EventType.STOCK_UPDATED)
                .productId(response.productId())
                .quantity(response.quantity())
                .availableQuantity(response.availableQuantity())
                .reservedQuantity(response.reservedQuantity())
                .build());
    }

    public void outOfStock(Long productId, Integer requestedQuantity,
                           Integer availableQuantity) {

        publish(OutOfStockEvent.builder()
                .eventType(EventType.OUT_OF_STOCK)
                .productId(productId)
                .quantity(requestedQuantity)
                .availableQuantity(availableQuantity)
                .requestedQuantity(requestedQuantity)
                .build());
    }

    private void publish(InventoryEvent event) {

        log.info("Publishing {} on topic {}", event.getEventType(),
                KafkaTopics.INVENTORY_UPDATED);

        kafkaTemplate.send(
                KafkaTopics.INVENTORY_UPDATED,
                event.getProductId().toString(),
                event
        );

        Counter.builder("kafka.stock.events.published")
                .description("Stock events published to Kafka")
                .register(meterRegistry)
                .increment();
    }
}