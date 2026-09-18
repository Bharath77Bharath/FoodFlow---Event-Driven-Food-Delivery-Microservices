package com.foodflow.notification_service.Kafka;

import com.foodflow.common.Event.*;
import com.foodflow.notification_service.Entity.Notification;
import com.foodflow.notification_service.Entity.NotificationChannel;
import com.foodflow.notification_service.Entity.NotificationType;
import com.foodflow.notification_service.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "order-events",
            groupId = "notification-service-group"
    )
    public void consumeOrderEvent(EventEnvelope envelope) {

        log.info(
                "Notification Service received event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (!EventType.ORDER_CREATED.name().equals(envelope.getEventType())) {
            return;
        }

        OrderCreatedEvent event =
                objectMapper.convertValue(
                        envelope.getData(),
                        OrderCreatedEvent.class
                );

        Notification notification =
                notificationService.createNotification(
                        envelope.getEventId(),
                        event.getUserId(),
                        event.getOrderId(),
                        NotificationType.ORDER_PLACED,
                        NotificationChannel.IN_APP,
                        "Your order #" + event.getOrderId() + " has been placed successfully."
                );

        if (notification != null) {
            notificationService.sendNotification(notification);
        }
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-service-group"
    )
    public void consumePaymentEvent(EventEnvelope envelope) {

        log.info(
                "Notification Service received payment event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (EventType.PAYMENT_SUCCESS.name().equals(envelope.getEventType())) {

            PaymentSuccessEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            PaymentSuccessEvent.class
                    );

            Notification notification =
                    notificationService.createNotification(
                            envelope.getEventId(),
                            event.getUserId(),
                            event.getOrderId(),
                            NotificationType.PAYMENT_SUCCESS,
                            NotificationChannel.IN_APP,
                            "Payment for order #" + event.getOrderId()
                                    + " was successful."
                    );

            if (notification != null) {
                notificationService.sendNotification(notification);
            }

        } else if (EventType.PAYMENT_FAILED.name().equals(envelope.getEventType())) {

            PaymentFailedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            PaymentFailedEvent.class
                    );

            Notification notification =
                    notificationService.createNotification(
                            envelope.getEventId(),
                            event.getUserId(),
                            event.getOrderId(),
                            NotificationType.PAYMENT_FAILED,
                            NotificationChannel.IN_APP,
                            "Payment for order #" + event.getOrderId()
                                    + " failed."
                    );

            if (notification != null) {
                notificationService.sendNotification(notification);
            }
        }
    }

    @KafkaListener(
            topics = "restaurant-events",
            groupId = "notification-service-group"
    )
    public void consumeRestaurantEvent(EventEnvelope envelope) {

        log.info(
                "Notification Service received restaurant event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (EventType.ORDER_CONFIRMED.name().equals(envelope.getEventType())) {

            OrderConfirmedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            OrderConfirmedEvent.class
                    );

            Notification notification =
                    notificationService.createNotification(
                            envelope.getEventId(),
                            event.getUserId(),
                            event.getOrderId(),
                            NotificationType.ORDER_CONFIRMED,
                            NotificationChannel.IN_APP,
                            "Your order #" + event.getOrderId()
                                    + " has been confirmed by the restaurant."
                    );

            if (notification != null) {
                notificationService.sendNotification(notification);
            }

        } else if (EventType.ORDER_ACCEPTED.name().equals(envelope.getEventType())) {

            OrderAcceptedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            OrderAcceptedEvent.class
                    );

            Notification notification =
                    notificationService.createNotification(
                            envelope.getEventId(),
                            event.getUserId(),
                            event.getOrderId(),
                            NotificationType.ORDER_PREPARING,
                            NotificationChannel.IN_APP,
                            "The restaurant has accepted your order #"
                                    + event.getOrderId()
                                    + " and is preparing it."
                    );

            if (notification != null) {
                notificationService.sendNotification(notification);
            }

        } else if (EventType.FOOD_READY.name().equals(envelope.getEventType())) {

            FoodReadyEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            FoodReadyEvent.class
                    );

            Notification notification =
                    notificationService.createNotification(
                            envelope.getEventId(),
                            event.getUserId(),
                            event.getOrderId(),
                            NotificationType.FOOD_READY,
                            NotificationChannel.IN_APP,
                            "Your order #" + event.getOrderId()
                                    + " is ready for pickup."
                    );

            if (notification != null) {
                notificationService.sendNotification(notification);
            }
        }
    }

    @KafkaListener(
            topics = "delivery-events",
            groupId = "notification-service-group"
    )
    public void consumeDeliveryEvent(EventEnvelope envelope) {

        log.info(
                "Notification Service received delivery event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (EventType.OUT_FOR_DELIVERY.name().equals(envelope.getEventType())) {

            OutForDeliveryEvent outForDeliveryEvent =
                    objectMapper.convertValue(
                            envelope.getData(),
                            OutForDeliveryEvent.class
                    );

            Notification notification1 =
                    notificationService.createNotification(
                            envelope.getEventId(),
                            outForDeliveryEvent.getUserId(),
                            outForDeliveryEvent.getOrderId(),
                            NotificationType.OUT_FOR_DELIVERY,
                            NotificationChannel.IN_APP,
                            "Your order #" + outForDeliveryEvent.getOrderId()
                                    + " is out for delivery."
                    );

            if (notification1 != null) {
                notificationService.sendNotification(notification1);
            }
        }

        else if (EventType.DELIVERED.name().equals(envelope.getEventType())) {

            DeliveredEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            DeliveredEvent.class
                    );

            Notification notification =
                    notificationService.createNotification(
                            envelope.getEventId(),
                            event.getUserId(),
                            event.getOrderId(),
                            NotificationType.ORDER_DELIVERED,
                            NotificationChannel.IN_APP,
                            "Your order #" + event.getOrderId()
                                    + " has been delivered. Enjoy your meal!"
                    );

            if (notification != null) {
                notificationService.sendNotification(notification);
            }
        }
    }
}
