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
        @JsonSubTypes.Type(value = CartAbandonedEvent.class, name = "cart-abandoned"),
        @JsonSubTypes.Type(value = CartCheckedOutEvent.class, name = "cart-checked-out")
})
public abstract class CartEvent extends DomainEvent {

    private Long customerId;

}