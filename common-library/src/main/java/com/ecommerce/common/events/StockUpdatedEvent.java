package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published on {@code inventory-updated} when the physical stock level of a
 * product changes (increase/decrease/confirm).
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class StockUpdatedEvent extends InventoryEvent {

}