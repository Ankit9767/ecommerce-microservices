package com.example.notification_service.exception;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long notificationId) {
        super(
                "Notification not found: " +
                        notificationId
        );
    }
}