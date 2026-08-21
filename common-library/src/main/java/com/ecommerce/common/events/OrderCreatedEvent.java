package com.ecommerce.common.events;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OrderCreatedEvent extends OrderEvent {

    private PaymentMethod paymentMethod;

    private BigDecimal totalAmount;

    private List<OrderItemDto> items;

}