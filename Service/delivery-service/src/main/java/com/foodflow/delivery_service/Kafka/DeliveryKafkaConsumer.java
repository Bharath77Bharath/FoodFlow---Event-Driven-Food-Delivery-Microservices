package com.foodflow.delivery_service.Kafka;

import com.foodflow.common.Event.EventEnvelope;
import com.foodflow.common.Event.EventType;
import com.foodflow.common.Event.FoodReadyEvent;
import com.foodflow.common.Event.PartnerAvailableEvent;
import com.foodflow.delivery_service.Dto.DeliveryResponseDto;
import com.foodflow.delivery_service.Exception.DuplicateDeliveryException;
import com.foodflow.delivery_service.Exception.NoAvailablePartnerException;
import com.foodflow.delivery_service.Repository.DeliveryPartnerRepo;
import com.foodflow.delivery_service.Service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final DeliveryPartnerRepo deliveryPartnerRepo;
    private final DeliveryService deliveryService;

    @KafkaListener(topics = "partner-events",groupId = "delivery-service")
    public void consumePartnerEvent(EventEnvelope envelope) {
        log.info(
                "Received partner event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if(EventType.PARTNER_AVAILABLE.name().equals(envelope.getEventType())) {
            PartnerAvailableEvent availableEvent = objectMapper.convertValue(envelope.getData(), PartnerAvailableEvent.class);

            log.info(
                    "Delivery Service received available partner: partnerId={}",
                    availableEvent.getDeliveryPartnerId()
            );
        }
        else {
            log.info(
                    "Delivery Service ignoring partner event type: {}",
                    envelope.getEventType()
            );
        }
    }

    @KafkaListener(topics = "restaurant-events",groupId = "delivery-service")
    public void consumeRestaurantEvents(EventEnvelope envelope) {
        log.info(
                "Received restaurant event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if(EventType.FOOD_READY.name().equals(envelope.getEventType())) {
            FoodReadyEvent foodReadyEvent = objectMapper.convertValue(envelope.getData(), FoodReadyEvent.class);

            log.info(
                    "Delivery Service received FoodReadyEvent: orderId={}, restaurantId={}, userId={}",
                    foodReadyEvent.getOrderId(),
                    foodReadyEvent.getRestaurantId(),
                    foodReadyEvent.getUserId()
            );

            try {
                DeliveryResponseDto deliveryResponseDto = deliveryService.createDeliveryFromFoodReady(foodReadyEvent.getOrderId(), foodReadyEvent.getRestaurantId(), foodReadyEvent.getUserId());

                log.info(
                        "Delivery assigned successfully: deliveryId={}, orderId={}, partnerId={}",
                        deliveryResponseDto.getId(),
                        deliveryResponseDto.getOrderId(),
                        deliveryResponseDto.getDeliveryPartnerId()
                );
            } catch (NoAvailablePartnerException e) {

                log.warn(
                        "No delivery partner available for order {}",
                        foodReadyEvent.getOrderId()
                );

            } catch (DuplicateDeliveryException e) {

                log.warn(
                        "Delivery already exists for order {}",
                        foodReadyEvent.getOrderId()
                );
            }
        }
        else {
            log.info(
                    "Delivery Service ignoring restaurant event type: {}",
                    envelope.getEventType()
            );
        }
    }
}
