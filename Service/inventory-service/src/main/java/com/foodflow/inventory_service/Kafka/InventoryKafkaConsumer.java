package com.foodflow.inventory_service.Kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.common.Event.*;
import com.foodflow.inventory_service.Exception.InsufficientInventoryException;
import com.foodflow.inventory_service.Exception.InvalidReservationException;
import com.foodflow.inventory_service.Exception.InventoryNotFoundException;
import com.foodflow.inventory_service.Service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryKafkaConsumer {

    private final InventoryService inventoryService;
    private final InventoryKafkaProducer inventoryKafkaProducer;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.ORDER_EVENTS,
            groupId = "inventory-service-group"
    )
    public void handleOrderCreated(EventEnvelope event) {

        if (!EventType.ORDER_CREATED.name().equals(event.getEventType())) {
            return;
        }

        OrderCreatedEvent orderCreatedEvent =
                objectMapper.convertValue(
                        event.getData(),
                        OrderCreatedEvent.class
                );

        try {

            inventoryService.reserveOrder(orderCreatedEvent);

            InventoryReservedEvent reservedEvent =
                    new InventoryReservedEvent(
                            orderCreatedEvent.getOrderId(),
                            orderCreatedEvent.getItems()
                    );

            EventEnvelope responseEvent =
                    new EventEnvelope(
                            UUID.randomUUID().toString(),
                            EventType.INVENTORY_RESERVED.name(),
                            LocalDateTime.now(),
                            "inventory-service",
                            reservedEvent
                    );

            inventoryKafkaProducer.publishInventoryReserved(responseEvent);

        } catch (InventoryNotFoundException
                 | InsufficientInventoryException
                 | InvalidReservationException e) {

            InventoryReservationFailedEvent failedEvent =
                    new InventoryReservationFailedEvent(
                            orderCreatedEvent.getOrderId(),
                            e.getMessage()
                    );

            EventEnvelope responseEvent =
                    new EventEnvelope(
                            UUID.randomUUID().toString(),
                            EventType.INVENTORY_RESERVATION_FAILED.name(),
                            LocalDateTime.now(),
                            "inventory-service",
                            failedEvent
                    );

            inventoryKafkaProducer.publishInventoryReservationFailed(
                    responseEvent
            );
        }
    }
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_EVENTS,
            groupId = "inventory-service-group"
    )
    public void handlePaymentFailed(EventEnvelope event) {

        if (!EventType.PAYMENT_FAILED.name().equals(event.getEventType())) {
            return;
        }

        PaymentFailedEvent paymentFailedEvent =
                objectMapper.convertValue(
                        event.getData(),
                        PaymentFailedEvent.class
                );

        Long orderId = paymentFailedEvent.getOrderId();

        List<OrderItemEvent> releasedItems =
                inventoryService.releaseReservations(orderId);

        InventoryReleasedEvent releasedEvent =
                new InventoryReleasedEvent(
                        orderId,
                        releasedItems
                );

        EventEnvelope responseEvent =
                new EventEnvelope(
                        UUID.randomUUID().toString(),
                        EventType.INVENTORY_RELEASED.name(),
                        LocalDateTime.now(),
                        "inventory-service",
                        releasedEvent
                );

        inventoryKafkaProducer.publishInventoryReleased(
                responseEvent
        );
    }

}
