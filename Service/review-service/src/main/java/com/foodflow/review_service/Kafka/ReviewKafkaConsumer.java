package com.foodflow.review_service.Kafka;

import com.foodflow.common.Event.DeliveredEvent;
import com.foodflow.common.Event.EventEnvelope;
import com.foodflow.common.Event.EventType;
import com.foodflow.review_service.Entity.ReviewEligibility;
import com.foodflow.review_service.Repository.ReviewEligibilityRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final ReviewEligibilityRepo reviewEligibilityRepo;

    @KafkaListener(
            topics = "delivery-events",
            groupId = "review-service"
    )
    public void consumeDeliveryEvent(EventEnvelope envelope) {

        log.info(
                "Review Service received event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (!EventType.DELIVERED.name().equals(envelope.getEventType())) {

            log.info(
                    "Review Service ignoring event type: {}",
                    envelope.getEventType()
            );

            return;
        }

        DeliveredEvent deliveredEvent =
                objectMapper.convertValue(
                        envelope.getData(),
                        DeliveredEvent.class
                );

        log.info(
                "Order delivered: orderId={}, userId={}, restaurantId={}",
                deliveredEvent.getOrderId(),
                deliveredEvent.getUserId(),
                deliveredEvent.getRestaurantId()
        );

        if (reviewEligibilityRepo.existsByOrderId(
                deliveredEvent.getOrderId())) {

            log.info(
                    "Review eligibility already exists for order {}",
                    deliveredEvent.getOrderId()
            );

            return;
        }

        ReviewEligibility eligibility = new ReviewEligibility();

        eligibility.setOrderId(deliveredEvent.getOrderId());
        eligibility.setUserId(deliveredEvent.getUserId());
        eligibility.setRestaurantId(deliveredEvent.getRestaurantId());
        eligibility.setDeliveredAt(LocalDateTime.now());

        reviewEligibilityRepo.save(eligibility);

        log.info(
                "Review eligibility created for order {}",
                deliveredEvent.getOrderId()
        );
    }
}