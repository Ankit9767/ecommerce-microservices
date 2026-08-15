package com.example.payment_service.dto.provider;

import java.math.BigDecimal;

public record PaymentProviderRequest(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String currency,
        String paymentMethod
) {
}
