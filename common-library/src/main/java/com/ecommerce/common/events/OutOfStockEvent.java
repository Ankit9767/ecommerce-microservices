package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OutOfStockEvent extends InventoryEvent {

    private Integer requestedQuantity;

    private Integer availableQuantity;

}