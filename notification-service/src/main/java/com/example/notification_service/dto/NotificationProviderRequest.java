package com.example.notification_service.dto;

public record NotificationProviderRequest(
        Long notificationId,
        String recipient,
        String subject,
        String message
) {
}