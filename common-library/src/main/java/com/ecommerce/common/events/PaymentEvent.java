package com.ecommerce.common.events;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

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
        @JsonSubTypes.Type(value = PaymentCompletedEvent.class, name = "payment-successful"),
        @JsonSubTypes.Type(value = PaymentCompletedEvent.class, name = "payment-failed")
})
public abstract class PaymentEvent extends DomainEvent {

    private Long paymentId;

    private Long orderId;

    private BigDecimal amount;

    private Currency currency;

    private PaymentMethod paymentMethod;

    private String transactionId;

}