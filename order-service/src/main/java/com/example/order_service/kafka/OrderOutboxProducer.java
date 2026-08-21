package com.example.order_service.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.events.OrderEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderOutboxProducer implements OutboxProducer {

    private final OrderKafkaProducer producer;

    @Override
    public void publish(DomainEvent event) {
        producer.publish((OrderEvent) event);
    }

    @Override
    public boolean supports(EventType eventType) {
        return eventType == EventType.ORDER_CREATED
                || eventType == EventType.ORDER_CANCELLED;
    }
}
