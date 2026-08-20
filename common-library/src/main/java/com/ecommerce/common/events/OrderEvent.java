package com.ecommerce.common.events;

import com.ecommerce.common.enums.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base contract for events published on the {@code order-created} topic.
 *
 * <p>Both {@link OrderCreatedEvent} and {@link OrderCancelledEvent} share this
 * topic; consumers bind the topic once on this base type and dispatch on
 * {@link DomainEvent#getEventType()}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class OrderEvent extends DomainEvent {

    private Long orderId;

    private Long customerId;

    private Currency currency;

}