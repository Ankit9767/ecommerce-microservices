package com.example.cart_service.kafka;

import com.ecommerce.common.events.CartEvent;
import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CartOutboxProducer implements OutboxProducer {

    private final CartEventProducer producer;

    @Override
    public CompletableFuture<?> publish(DomainEvent event) {

       return producer.publish((CartEvent) event);
    }

    @Override
    public boolean supports(EventType eventType) {

        return eventType == EventType.CART_CHECKED_OUT
                || eventType == EventType.CART_ABANDONED;
    }
}