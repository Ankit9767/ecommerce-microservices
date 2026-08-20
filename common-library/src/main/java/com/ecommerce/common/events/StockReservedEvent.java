package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published on {@code inventory-updated} when stock transitions.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class StockReservedEvent extends InventoryEvent {

}