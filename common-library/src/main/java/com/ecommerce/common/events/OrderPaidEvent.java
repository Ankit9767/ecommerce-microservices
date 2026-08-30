package com.ecommerce.common.events;

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
public class OrderPaidEvent extends OrderEvent {

    private BigDecimal totalAmount;

    private PaymentMethod paymentMethod;

    private List<OrderItemDto> items;

    private String customerEmail;
}