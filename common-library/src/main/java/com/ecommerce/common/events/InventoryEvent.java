package com.ecommerce.common.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
        @JsonSubTypes.Type(value = StockReservedEvent.class, name = "stock-reserved"),
        @JsonSubTypes.Type(value = StockReleasedEvent.class, name = "stock-released"),
        @JsonSubTypes.Type(value = StockUpdatedEvent.class, name = "stock-updated"),
        @JsonSubTypes.Type(value = OutOfStockEvent.class, name = "out-of-stock")
})
public abstract class InventoryEvent extends DomainEvent {

    private Long productId;

    private Integer quantity;

    private Integer availableQuantity;

    private Integer reservedQuantity;

    private Long orderItemId;

}