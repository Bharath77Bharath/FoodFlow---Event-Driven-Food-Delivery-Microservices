package com.foodflow.notification_service.Dto;

import com.foodflow.notification_service.Entity.NotificationChannel;
import com.foodflow.notification_service.Entity.NotificationStatus;
import com.foodflow.notification_service.Entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private String eventId;

    private Long userId;

    private Long orderId;

    private NotificationType type;

    private NotificationChannel channel;

    private String message;

    private NotificationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;
}
