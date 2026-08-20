package com.ecommerce.common.events;

import com.ecommerce.common.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Published on {@code payment-completed} when a payment resolves to a
 * final state.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PaymentCompletedEvent extends PaymentEvent {

    private PaymentStatus paymentStatus;

    private String failureReason;

}