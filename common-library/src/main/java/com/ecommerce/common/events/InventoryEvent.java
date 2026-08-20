package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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