package com.example.payment_service.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.events.PaymentEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class PaymentOutboxProducer implements OutboxProducer {

    private final PaymentCompletedProducer producer;

    @Override
    public CompletableFuture<?> publish(DomainEvent event) {
        return producer.publish((PaymentEvent) event);
    }

    @Override
    public boolean supports(EventType eventType) {
        return eventType == EventType.PAYMENT_SUCCESSFUL
                || eventType == EventType.PAYMENT_FAILED;
    }
}
