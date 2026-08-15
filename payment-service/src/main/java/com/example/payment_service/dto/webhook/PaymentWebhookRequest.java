package com.example.payment_service.dto.webhook;

import com.ecommerce.common.enums.PaymentStatus;

public record PaymentWebhookRequest(
        String provider,
        String providerReference,
        PaymentStatus status,
        String failureReason
) {
}