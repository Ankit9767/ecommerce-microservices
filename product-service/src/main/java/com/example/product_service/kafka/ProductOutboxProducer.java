package com.example.product_service.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.events.ProductEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ProductOutboxProducer implements OutboxProducer {

    private final ProductEventProducer producer;

    @Override
    public CompletableFuture<?> publish(DomainEvent event) {

        return producer.publish((ProductEvent) event);
    }

    @Override
    public boolean supports(EventType eventType) {

        return eventType == EventType.PRODUCT_CREATED
                || eventType == EventType.PRODUCT_UPDATED
                || eventType == EventType.PRODUCT_DELETED;
    }
}