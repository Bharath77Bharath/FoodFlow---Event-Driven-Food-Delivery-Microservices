package com.foodflow.restaurantservice.Kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.common.Event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final RestaurantKafkaProducer restaurantKafkaProducer;

    @KafkaListener(
            topics = "order-events",
            groupId = "restaurant-service"
    )
    public void consumeOrderEvent(EventEnvelope envelope) {

        log.info(
                "Received event: eventId={}, eventType={}, source={}",
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

            log.info(
                    "Restaurant received confirmed order: orderId={}, restaurantId={}, amount={}",
                    event.getOrderId(),
                    event.getRestaurantId(),
                    event.getTotalAmount()
            );

            try {
                log.info(
                        "Restaurant is preparing to accept order {}. Waiting 10 seconds...",
                        event.getOrderId()
                );

                Thread.sleep(10_000);

                OrderAcceptedEvent acceptedEvent = new OrderAcceptedEvent(
                        event.getOrderId(),
                        event.getRestaurantId()
                );

                restaurantKafkaProducer.publishOrderAccepted(acceptedEvent);

                log.info(
                        "Restaurant accepted order {} after 10 seconds",
                        event.getOrderId()
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                log.error(
                        "Restaurant processing interrupted for order {}",
                        event.getOrderId(),
                        e
                );
            }

            try {
                log.info(
                        "Restaurant is preparing food for order {}. Waiting another 10 seconds...",
                        event.getOrderId()
                );

                Thread.sleep(10_000);

                FoodReadyEvent foodReadyEvent =
                        new FoodReadyEvent(
                                event.getOrderId(),
                                event.getRestaurantId()
                        );

                restaurantKafkaProducer.publishFoodReady(foodReadyEvent);

                log.info(
                        "Food is ready for order {} after another 10 seconds",
                        event.getOrderId()
                );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                log.error(
                        "Restaurant food preparation interrupted for order {}",
                        event.getOrderId(),
                        e
                );
            }

        } else {
            log.info(
                    "Restaurant Service ignoring event type: {}",
                    envelope.getEventType()
            );
        }
    }
}