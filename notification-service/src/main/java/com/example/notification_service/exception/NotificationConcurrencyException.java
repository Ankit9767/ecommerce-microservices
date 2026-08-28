package com.example.notification_service.exception;

public class NotificationConcurrencyException extends RuntimeException {

    public NotificationConcurrencyException(Long notificationId) {

        super(
                "Concurrent modification detected for notification: "
                        + notificationId
        );
    }
}