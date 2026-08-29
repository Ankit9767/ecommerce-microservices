package com.example.notification_service.exception;

public class InvalidNotificationMessageException
        extends NotificationProviderException {

    public InvalidNotificationMessageException() {
        super("Notification message must not be blank");
    }
}
