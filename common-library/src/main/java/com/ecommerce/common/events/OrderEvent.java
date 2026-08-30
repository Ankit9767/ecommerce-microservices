package com.ecommerce.common.events;

import com.ecommerce.common.enums.Currency;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base contract for events published on the {@code order-created} topic.
 *
 * <p>Explicitly polymorphic so the concrete subclass can be reconstructed from
 * the payload's {@code eventType} discriminator alone, with no Kafka type
 * headers required.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "order-created"),
        @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "order-cancelled"),
        @JsonSubTypes.Type(value = OrderPaidEvent.class, name = "order-paid")
})
public abstract class OrderEvent extends DomainEvent {

    private Long orderId;

    private Long customerId;

    private Currency currency;

    private String recipientEmail;

}