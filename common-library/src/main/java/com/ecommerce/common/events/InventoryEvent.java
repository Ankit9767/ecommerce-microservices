package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base contract for events published on the {@code inventory-updated} topic.
 *
 * <p>All stock-related variants share this topic; consumers bind the topic once
 * on this base type and dispatch on {@link DomainEvent#getEventType()}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class InventoryEvent extends DomainEvent {

    private Long productId;

    private Integer quantity;

    private Integer availableQuantity;

    private Integer reservedQuantity;

    private Long orderItemId;

}