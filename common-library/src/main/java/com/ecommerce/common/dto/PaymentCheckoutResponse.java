package com.ecommerce.common.dto;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentCheckoutResponse(
        Long paymentId,
        Long orderId,
        String provider,
        String providerOrderId,
        String providerKeyId,
        BigDecimal amount,
        Currency currency,
        PaymentStatus status
) {
}