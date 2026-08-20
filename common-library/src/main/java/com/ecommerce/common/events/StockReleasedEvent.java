package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published on {@code inventory-updated} when previously reserved stock is
 * released.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class StockReleasedEvent extends InventoryEvent {

}