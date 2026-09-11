package com.foodflow.paymentservice.Kafka;

import com.foodflow.common.Event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void consumeOrderCreated(EventEnvelope envelope) {

        log.info(
                "Received event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if(EventType.ORDER_CREATED.name().equals(envelope.getEventType())) {
            OrderCreatedEvent event = objectMapper.convertValue(envelope.getData(), OrderCreatedEvent.class);

            log.info(
                    "Received OrderCreatedEvent: orderId={}, userId={}, restaurantId={}, totalAmount={}",
                    event.getOrderId(),
                    event.getUserId(),
                    event.getRestaurantId(),
                    event.getTotalAmount()
            );
        } else {
            log.warn(
                    "Ignoring unsupported event type: {}",
                    envelope.getEventType()
            );
        }
    }
}
