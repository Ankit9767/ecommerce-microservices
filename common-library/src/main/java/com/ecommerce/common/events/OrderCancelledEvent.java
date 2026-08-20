package com.ecommerce.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published on {@code order-created} when an order is cancelled.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OrderCancelledEvent extends OrderEvent {

    private String reason;

}