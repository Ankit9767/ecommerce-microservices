package com.ecommerce.common.events;

import com.ecommerce.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Published on {@code order-created} when an order is placed.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OrderCreatedEvent extends OrderEvent {

    private Long productId;

    private Integer quantity;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

}