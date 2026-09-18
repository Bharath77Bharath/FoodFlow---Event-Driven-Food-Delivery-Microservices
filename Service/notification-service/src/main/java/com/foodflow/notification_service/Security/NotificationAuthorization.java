package com.foodflow.notification_service.Security;

import com.foodflow.notification_service.Entity.Notification;
import com.foodflow.notification_service.Repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("notificationAuthorization")
@RequiredArgsConstructor
public class NotificationAuthorization {

    private final NotificationRepo notificationRepo;

    public boolean isOwner(Long notificationId) {

        Notification notification = notificationRepo
                .findById(notificationId)
                .orElse(null);

        if (notification == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(notification.getUserId());
    }

    public boolean isOwnerByUserId(Long userId) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(userId);
    }

    public boolean isOwnerByOrderId(Long orderId) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return notificationRepo
                .existsByOrderIdAndUserId(orderId, currentUserId);
    }
}