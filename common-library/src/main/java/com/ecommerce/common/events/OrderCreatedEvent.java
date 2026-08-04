package com.ecommerce.common.events;

import com.ecommerce.common.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

    private Long orderId;

    private Long customerId;

    private Long productId;

    private Integer quantity;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;
}