package com.ecommerce.common.events;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class PaymentEvent extends DomainEvent {

    private Long paymentId;

    private Long orderId;

    private BigDecimal amount;

    private Currency currency;

    private PaymentMethod paymentMethod;

    private String transactionId;

}