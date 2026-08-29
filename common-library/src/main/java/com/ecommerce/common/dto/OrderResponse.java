package com.ecommerce.common.dto;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.OrderStatus;
import com.ecommerce.common.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    private Long customerId;

    private String customerEmail;

    private BigDecimal totalAmount;

    private Currency currency;

    private PaymentMethod paymentMethod;

    private OrderStatus status;

    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}