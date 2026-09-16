package com.foodflow.notification_service.Controller;

import com.foodflow.notification_service.Dto.NotificationResponse;
import com.foodflow.notification_service.Service.NotificationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long notificationId) {
        NotificationResponse response = notificationService.getNotificationById(notificationId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationByUserId(@PathVariable Long userId) {
        List<NotificationResponse> responseList = notificationService.getNotificationsByUserId(userId);

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationByOrderId(@PathVariable Long orderId) {
        List<NotificationResponse> responseList = notificationService.getNotificationsByOrderId(orderId);

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }
}
