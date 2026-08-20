package com.ecommerce.common.events;

import com.ecommerce.common.kafka.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CartAbandonedEvent extends CartEvent {

    public static CartEvent of(Long customerId) {
        return CartAbandonedEvent.builder()
                .eventType(EventType.CART_ABANDONED)
                .customerId(customerId)
                .build();
    }
}