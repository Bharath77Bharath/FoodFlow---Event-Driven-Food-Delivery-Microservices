package com.foodflow.review_service.Kafka;

import com.foodflow.common.Event.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewKafkaProducer {

    private static final String REVIEW_TOPIC = "review-events";

    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

    public void publishReviewCreated(EventEnvelope event) {

        kafkaTemplate.send(
                REVIEW_TOPIC,
                event.getEventId(),
                event
        );
    }
}