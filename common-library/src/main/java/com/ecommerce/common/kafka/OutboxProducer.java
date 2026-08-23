package com.ecommerce.common.kafka;

import com.ecommerce.common.events.DomainEvent;

import java.util.concurrent.CompletableFuture;

public interface OutboxProducer {

    CompletableFuture<?> publish(DomainEvent event);

    boolean supports(EventType eventType);
}
