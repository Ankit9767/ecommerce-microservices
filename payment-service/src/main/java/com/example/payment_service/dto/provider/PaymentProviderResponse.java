package com.example.payment_service.dto.provider;

import com.ecommerce.common.enums.PaymentStatus;

public record PaymentProviderResponse(
        String providerPaymentId,
        PaymentStatus status
) {
}
