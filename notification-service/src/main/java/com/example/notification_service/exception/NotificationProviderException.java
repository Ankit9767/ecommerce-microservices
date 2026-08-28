package com.example.notification_service.exception;

public class NotificationProviderException extends RuntimeException {

    public NotificationProviderException(String message) {

        super(message);
    }

    public NotificationProviderException(String message,
                                         Throwable cause) {

        super(message, cause);
    }
}