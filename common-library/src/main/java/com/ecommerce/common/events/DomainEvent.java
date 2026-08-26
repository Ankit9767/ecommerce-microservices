package com.ecommerce.common.events;

import com.ecommerce.common.kafka.EventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DomainEvent {

    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    private EventType eventType;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}