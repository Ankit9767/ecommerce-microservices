package com.example.notification_service.exception;

public class InvalidNotificationSubjectException
        extends NotificationProviderException {

    public InvalidNotificationSubjectException() {
        super("Notification subject must not be blank");
    }
}
