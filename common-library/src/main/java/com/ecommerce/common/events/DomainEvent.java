package com.ecommerce.common.events;

import com.ecommerce.common.kafka.EventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base contract for every event produced on the shared Kafka topics.
 *
 * <p>The {@link EventType} discriminator drives coarse-topic routing: a single
 * consumer binds one topic and dispatches on the event type. It serializes to
 * its stable kebab-case value on the wire, so the Kafka payload format is
 * unchanged.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DomainEvent {

    private EventType eventType;

}