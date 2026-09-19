package com.ecommerce.common.dto;

import com.ecommerce.common.enums.PaymentStatus;

public record PaymentProviderResponse(
        String providerOrderId,
        String providerPaymentId,
        String providerReference,
        PaymentStatus status,
        String failureReason
) {
}
