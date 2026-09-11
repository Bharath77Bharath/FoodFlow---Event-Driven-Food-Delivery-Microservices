package com.foodflow.paymentservice.Kafka;

import com.foodflow.common.Event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentKafkaProducer {

    private static final String PAYMENT_TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCreated(PaymentCreatedEvent event) {

        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.PAYMENT_CREATED.name(),
                LocalDateTime.now(),
                "payment-service",
                event
        );

        kafkaTemplate.send(
                PAYMENT_TOPIC,
                event.getOrderId().toString(),
                envelope
        );
    }

    public void publishPaymentSuccess(PaymentSuccessEvent event) {

        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.PAYMENT_SUCCESS.name(),
                LocalDateTime.now(),
                "payment-service",
                event
        );

        kafkaTemplate.send(
            PAYMENT_TOPIC,
            event.getOrderId().toString(),
            envelope
        );
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {

        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID().toString(),
                EventType.PAYMENT_FAILED.name(),
                LocalDateTime.now(),
                "payment-service",
                event
        );

        kafkaTemplate.send(
                PAYMENT_TOPIC,
                event.getOrderId().toString(),
                envelope
        );
    }

}
