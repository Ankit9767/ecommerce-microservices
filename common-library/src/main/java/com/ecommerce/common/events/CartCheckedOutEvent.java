package com.ecommerce.common.events;

import com.ecommerce.common.kafka.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published when a cart is checked out (publish-only this milestone).
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CartCheckedOutEvent extends CartEvent {

    public static CartEvent of(Long customerId) {
        return CartCheckedOutEvent.builder()
                .eventType(EventType.CART_CHECKED_OUT)
                .customerId(customerId)
                .build();
    }
}