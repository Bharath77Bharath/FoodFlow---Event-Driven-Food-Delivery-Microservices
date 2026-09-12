package com.foodflow.delivery_service.Kafka;

import com.foodflow.common.Event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryKafkaProducer {

    private static final String PARTNER_TOPIC = "partner-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPartnerAvailable(PartnerAvailableEvent event) {
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.PARTNER_AVAILABLE.name(),
                LocalDateTime.now(),
                "delivery-service",
                event
        );

        kafkaTemplate.send(
                PARTNER_TOPIC,
                event.getDeliveryPartnerId().toString(),
                envelope
        );
    }

    public void publishDeliveryAssigned(DeliveryAssignedEvent event) {
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.DELIVERY_ASSIGNED.name(),
                LocalDateTime.now(),
                "delivery-service",
                event
        );

        kafkaTemplate.send(
                PARTNER_TOPIC,
                event.getOrderId().toString(),
                envelope
        );
    }

    public void publishOutForDelivery(OutForDeliveryEvent event) {
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.OUT_FOR_DELIVERY.name(),
                LocalDateTime.now(),
                "delivery-service",
                event
        );

        kafkaTemplate.send(
                "delivery-events",
                event.getOrderId().toString(),
                envelope
        );
    }

    public void publishDelivered(DeliveredEvent event) {

        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.DELIVERED.name(),
                LocalDateTime.now(),
                "delivery-service",
                event
        );

        kafkaTemplate.send(
                "delivery-events",
                event.getOrderId().toString(),
                envelope
        );
    }
}
