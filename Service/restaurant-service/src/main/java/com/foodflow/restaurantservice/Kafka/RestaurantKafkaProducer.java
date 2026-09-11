package com.foodflow.restaurantservice.Kafka;

import com.foodflow.common.Event.EventEnvelope;
import com.foodflow.common.Event.EventType;
import com.foodflow.common.Event.FoodReadyEvent;
import com.foodflow.common.Event.OrderAcceptedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantKafkaProducer {

    private static final String RESTAURANT_TOPIC = "restaurant-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderAccepted(OrderAcceptedEvent event) {
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.ORDER_ACCEPTED.name(),
                LocalDateTime.now(),
                "restaurant-service",
                event
        );

        kafkaTemplate.send(
                RESTAURANT_TOPIC,
                event.getOrderId().toString(),
                envelope
        );

    }

    public void publishFoodReady(FoodReadyEvent event) {
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.FOOD_READY.name(),
                LocalDateTime.now(),
                "restaurant-service",
                event
        );

        kafkaTemplate.send(
                RESTAURANT_TOPIC,
                event.getOrderId().toString(),
                envelope
        );
    }
}
