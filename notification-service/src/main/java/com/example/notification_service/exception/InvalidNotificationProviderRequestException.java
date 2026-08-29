package com.example.notification_service.exception;

public class InvalidNotificationProviderRequestException
        extends NotificationProviderException {

    public InvalidNotificationProviderRequestException() {
        super("Notification provider request must not be null");
    }
}
