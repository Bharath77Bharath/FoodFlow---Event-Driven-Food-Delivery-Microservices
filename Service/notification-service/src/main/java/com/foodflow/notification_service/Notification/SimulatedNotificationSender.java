package com.foodflow.notification_service.Notification;

import com.foodflow.notification_service.Entity.Notification;
import com.foodflow.notification_service.Exception.NotificationSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SimulatedNotificationSender implements NotificationSender {

    @Override
    public void send(Notification notification) {

        try {

            log.info(
                    "Sending {} notification to user {}: {}",
                    notification.getChannel(),
                    notification.getUserId(),
                    notification.getMessage()
            );

            // Simulate notification sending
            Thread.sleep(500);

            log.info(
                    "Notification sent successfully. notificationId={}",
                    notification.getId()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new NotificationSendException(
                    "Notification sending interrupted for notification: "
                            + notification.getId()
            );
        }
    }

}
