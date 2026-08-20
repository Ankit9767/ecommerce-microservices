package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published on {@code inventory-updated} when a reservation cannot be fulfilled
 * because the available stock is insufficient.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OutOfStockEvent extends InventoryEvent {

    private Integer requestedQuantity;

    private Integer availableQuantity;

}