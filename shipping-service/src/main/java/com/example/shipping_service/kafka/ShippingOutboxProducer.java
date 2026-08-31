package com.example.shipping_service.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.events.ShipmentEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class ShippingOutboxProducer implements OutboxProducer {

    private final ShipmentEventProducer producer;

    @Override
    public CompletableFuture<?> publish(DomainEvent event) {

        return producer.publish((ShipmentEvent) event);
    }

    @Override
    public boolean supports(EventType eventType) {

        return eventType == EventType.SHIPMENT_CREATED
                || eventType == EventType.SHIPMENT_SHIPPED
                || eventType == EventType.SHIPMENT_IN_TRANSIT
                || eventType == EventType.SHIPMENT_OUT_FOR_DELIVERY
                || eventType == EventType.SHIPMENT_DELIVERED
                || eventType == EventType.SHIPMENT_FAILED
                || eventType == EventType.SHIPMENT_CANCELLED;
    }
}