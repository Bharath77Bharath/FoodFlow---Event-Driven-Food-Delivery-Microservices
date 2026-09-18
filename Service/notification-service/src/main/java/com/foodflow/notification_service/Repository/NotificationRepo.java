package com.foodflow.notification_service.Repository;

import com.foodflow.notification_service.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    boolean existsByEventId(String eventId);

    boolean existsByOrderIdAndUserId(Long orderId, Long userId);
}
