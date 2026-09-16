package com.foodflow.notification_service.Notification;

import com.foodflow.notification_service.Entity.Notification;
import org.springframework.stereotype.Component;

@Component
public interface NotificationSender {

    public void send(Notification notification);
}
