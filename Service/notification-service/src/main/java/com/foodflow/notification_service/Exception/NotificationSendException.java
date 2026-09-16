package com.foodflow.notification_service.Exception;

public class NotificationSendException extends RuntimeException {
    public NotificationSendException(String message) {
        super(message);
    }
}
