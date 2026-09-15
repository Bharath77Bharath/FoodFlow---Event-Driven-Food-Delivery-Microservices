package com.foodflow.inventory_service.Kafka;

import com.foodflow.common.Event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryKafkaProducer {

    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

    public void publishInventoryReserved(
            EventEnvelope event
    ) {
        kafkaTemplate.send(
                KafkaTopics.INVENTORY_EVENTS,
                event.getEventId(),
                event
        );
    }

    public void publishInventoryReservationFailed(
            EventEnvelope event
    ) {
        kafkaTemplate.send(
                KafkaTopics.INVENTORY_EVENTS,
                event.getEventId(),
                event
        );
    }

    public void publishInventoryReleased(
            EventEnvelope event
    ) {
        kafkaTemplate.send(
                KafkaTopics.INVENTORY_EVENTS,
                event.getEventId(),
                event
        );
    }
}