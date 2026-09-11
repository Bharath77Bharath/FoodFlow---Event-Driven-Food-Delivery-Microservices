package com.foodflow.order_service.Kafka;

import com.foodflow.common.Event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private static final String ORDER_TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {

        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.ORDER_CREATED.name(),
                LocalDateTime.now(),
                "order-service",
                event
        );

        kafkaTemplate.send(
                ORDER_TOPIC,
                event.getOrderId().toString(),
                envelope
        );
    }

    public void publishOrderConfirmed(OrderConfirmedEvent event) {

        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.ORDER_CONFIRMED.name(),
                LocalDateTime.now(),
                "order-service",
                event
        );

        kafkaTemplate.send(
                ORDER_TOPIC,
                event.getOrderId().toString(),
                envelope
        );
    }
}
