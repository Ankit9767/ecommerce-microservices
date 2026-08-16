package com.example.payment_service.dto.provider;

import com.ecommerce.common.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentProviderRequest(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod
) {
}
