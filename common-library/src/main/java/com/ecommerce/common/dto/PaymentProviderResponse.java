package com.ecommerce.common.dto;

import com.ecommerce.common.enums.PaymentStatus;

public record PaymentProviderResponse(
        String providerReference,
        PaymentStatus status,
        String failureReason
) {
}
