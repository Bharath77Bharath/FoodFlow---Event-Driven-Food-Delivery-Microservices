package com.foodflow.notification_service.Service;

import com.foodflow.notification_service.Dto.NotificationResponse;
import com.foodflow.notification_service.Entity.Notification;
import com.foodflow.notification_service.Entity.NotificationChannel;
import com.foodflow.notification_service.Entity.NotificationStatus;
import com.foodflow.notification_service.Entity.NotificationType;
import com.foodflow.notification_service.Exception.NotificationNotFoundException;
import com.foodflow.notification_service.Repository.NotificationRepo;
import com.foodflow.notification_service.Notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepo notificationRepository;
    private final NotificationSender notificationSender;


    public NotificationResponse getNotificationById(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + notificationId
                        )
                );

        return mapToResponse(notification);
    }

    public List<NotificationResponse> getNotificationsByUserId(Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<NotificationResponse> getNotificationsByOrderId(Long orderId) {

        return notificationRepository
                .findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public Notification createNotification(
            String eventId,
            Long userId,
            Long orderId,
            NotificationType type,
            NotificationChannel channel,
            String message) {

        // Idempotency check
        if (notificationRepository.existsByEventId(eventId)) {
            return null;
        }

        Notification notification = Notification.builder()
                .eventId(eventId)
                .userId(userId)
                .orderId(orderId)
                .type(type)
                .channel(channel)
                .message(message)
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    public void sendNotification(Notification notification) {

        try {

            notificationSender.send(notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

            notificationRepository.save(notification);

        } catch (Exception e) {

            notification.setStatus(NotificationStatus.FAILED);

            notificationRepository.save(notification);

            throw e;
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .userId(notification.getUserId())
                .orderId(notification.getOrderId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}
