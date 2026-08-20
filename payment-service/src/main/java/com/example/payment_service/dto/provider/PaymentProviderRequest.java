package com.example.payment_service.dto.provider;

import com.ecommerce.common.enums.Currency;
import com.ecommerce.common.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentProviderRequest(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        Currency currency,
        PaymentMethod paymentMethod
) {
}
