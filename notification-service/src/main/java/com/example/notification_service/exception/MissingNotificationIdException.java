package com.example.notification_service.exception;

public class MissingNotificationIdException
        extends NotificationProviderException {

    public MissingNotificationIdException() {
        super("Notification provider request must contain notificationId");
    }
}
