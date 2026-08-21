package com.ecommerce.common.kafka;

import com.ecommerce.common.events.DomainEvent;

public interface OutboxProducer {

    void publish(DomainEvent event);

    boolean supports(EventType eventType);
}
