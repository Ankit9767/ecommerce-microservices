package com.example.notification_service.exception;

public class InvalidNotificationRecipientException
        extends NotificationProviderException {

    public InvalidNotificationRecipientException() {
        super("Notification recipient must not be blank");
    }
}
