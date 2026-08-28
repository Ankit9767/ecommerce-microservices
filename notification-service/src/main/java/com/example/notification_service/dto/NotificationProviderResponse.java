package com.example.notification_service.dto;

public record NotificationProviderResponse(
        String providerReference,
        boolean successful,
        String failureReason
) {
}