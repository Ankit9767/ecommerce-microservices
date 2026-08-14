package com.ecommerce.common.dto;

import com.ecommerce.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long id,

        Long orderId,

        Long customerId,

        BigDecimal amount,

        String currency,

        PaymentStatus status,

        String paymentMethod,

        String provider,

        String providerReference,

        String failureReason,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}