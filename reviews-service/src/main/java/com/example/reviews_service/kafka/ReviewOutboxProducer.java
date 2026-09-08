package com.example.reviews_service.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ReviewOutboxProducer implements OutboxProducer {

    private final ReviewEventProducer producer;

    @Override
    public CompletableFuture<?> publish(DomainEvent event) {
        return producer.publish((ReviewCreatedEvent) event);
    }

    @Override
    public boolean supports(EventType eventType) {
        return eventType == EventType.REVIEW_CREATED;
    }
}