package com.foodflow.order_service.Kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.order_service.Entity.Order;
import com.foodflow.order_service.Entity.OrderStatus;
import com.foodflow.common.Event.*;
import com.foodflow.order_service.Exception.OrderNotFoundException;
import com.foodflow.order_service.Repository.OrderRepo;
import com.foodflow.order_service.Security.EventAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final OrderRepo orderRepo;
    private final ObjectMapper objectMapper;
    private final KafkaProducerService kafkaProducerService;
    private final EventAuthorization eventAuthorization;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service"
    )
    public void consumePaymentEvent(EventEnvelope envelope) {

        log.info(
                "Received event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );
        if (!eventAuthorization.isFrom(
                envelope.getSource(),
                "payment-service")) {

            log.warn(
                    "Rejected payment event from unauthorized source: {}",
                    envelope.getSource()
            );

            return;
        }

        if (EventType.PAYMENT_CREATED.name().equals(envelope.getEventType())) {

            PaymentCreatedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            PaymentCreatedEvent.class
                    );

            handlePaymentCreated(event);

        } else if (EventType.PAYMENT_SUCCESS.name().equals(envelope.getEventType())) {

            PaymentSuccessEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            PaymentSuccessEvent.class
                    );

            handlePaymentSuccess(event);

        } else if (EventType.PAYMENT_FAILED.name().equals(envelope.getEventType())) {

            PaymentFailedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            PaymentFailedEvent.class
                    );

            handlePaymentFailed(event);

        } else {

            log.warn(
                    "Unknown payment event type: {}",
                    envelope.getEventType()
            );
        }
    }

    @KafkaListener(topics = "restaurant-events",groupId = "order-service")
    public void consumeRestaurantEvent(EventEnvelope envelope) {
        log.info(
                "Received restaurant event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (!eventAuthorization.isFrom(
                envelope.getSource(),
                "restaurant-service")) {

            log.warn(
                    "Rejected restaurant event from unauthorized source: {}",
                    envelope.getSource()
            );

            return;
        }

        if(EventType.ORDER_ACCEPTED.name().equals(envelope.getEventType())) {
            OrderAcceptedEvent event = objectMapper.convertValue(
                    envelope.getData(),
                    OrderAcceptedEvent.class
            );

            log.info(
                    "Received OrderAcceptedEvent: orderId={}, restaurantId={}",
                    event.getOrderId(),
                    event.getRestaurantId()
            );

            handleOrderAccepted(event);
        }
        else if (EventType.FOOD_READY.name().equals(envelope.getEventType())) {

            FoodReadyEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            FoodReadyEvent.class
                    );

            log.info(
                    "Received FoodReadyEvent: orderId={}, restaurantId={}",
                    event.getOrderId(),
                    event.getRestaurantId()
            );

            handleFoodReady(event);

        }
        else {
            log.info(
                    "Order Service ignoring restaurant event type: {}",
                    envelope.getEventType()
            );
        }
    }

    @KafkaListener(
            topics = "delivery-events",
            groupId = "order-service"
    )
    public void consumeDeliveryEvent(EventEnvelope envelope) {

        log.info(
                "Received delivery event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (!eventAuthorization.isFrom(
                envelope.getSource(),
                "delivery-service")) {

            log.warn(
                    "Rejected delivery event from unauthorized source: {}",
                    envelope.getSource()
            );

            return;
        }

        if (EventType.OUT_FOR_DELIVERY.name()
                .equals(envelope.getEventType())) {

            OutForDeliveryEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            OutForDeliveryEvent.class
                    );

            log.info(
                    "Received OutForDeliveryEvent: orderId={}, deliveryId={}, partnerId={}",
                    event.getOrderId(),
                    event.getDeliveryId(),
                    event.getDeliveryPartnerId()
            );

            handleOutForDelivery(event);

        }

        else if (EventType.DELIVERED.name().equals(envelope.getEventType())) {

            DeliveredEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            DeliveredEvent.class
                    );

            log.info(
                    "Received DeliveredEvent: orderId={}, deliveryId={}, partnerId={}",
                    event.getOrderId(),
                    event.getDeliveryId(),
                    event.getDeliveryPartnerId()
            );

            handleDelivered(event);
        }

        else {

            log.info(
                    "Order Service ignoring delivery event type: {}",
                    envelope.getEventType()
            );
        }
    }

    @KafkaListener(
            topics = "inventory-events",
            groupId = "order-service"
    )
    public void consumeInventoryEvent(EventEnvelope envelope) {

        log.info(
                "Received inventory event: eventId={}, eventType={}, source={}",
                envelope.getEventId(),
                envelope.getEventType(),
                envelope.getSource()
        );

        if (!eventAuthorization.isFrom(
                envelope.getSource(),
                "inventory-service")) {

            log.warn(
                    "Rejected inventory event from unauthorized source: {}",
                    envelope.getSource()
            );

            return;
        }

        if (EventType.INVENTORY_RESERVED.name()
                .equals(envelope.getEventType())) {

            InventoryReservedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            InventoryReservedEvent.class
                    );

            log.info(
                    "Received InventoryReservedEvent: orderId={}",
                    event.getOrderId()
            );

            handleInventoryReserved(event);
        }

        else if (EventType.INVENTORY_RESERVATION_FAILED.name()
                .equals(envelope.getEventType())) {

            InventoryReservationFailedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            InventoryReservationFailedEvent.class
                    );

            log.info(
                    "Received InventoryReservationFailedEvent: orderId={}, reason={}",
                    event.getOrderId(),
                    event.getReason()
            );

            handleInventoryReservationFailed(event);
        }

        else if (EventType.INVENTORY_RELEASED.name()
                .equals(envelope.getEventType())) {

            InventoryReleasedEvent event =
                    objectMapper.convertValue(
                            envelope.getData(),
                            InventoryReleasedEvent.class
                    );

            log.info(
                    "Received InventoryReleasedEvent: orderId={}",
                    event.getOrderId()
            );

            handleInventoryReleased(event);
        }

        else {

            log.info(
                    "Order Service ignoring inventory event type: {}",
                    envelope.getEventType()
            );
        }
    }





    //helper methods

    private void handlePaymentCreated(PaymentCreatedEvent event) {

        log.info(
                "Received PaymentCreatedEvent: paymentId={}, orderId={}, userId={}, amount={}",
                event.getPaymentId(),
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount()
        );

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        if (order.getStatus() != OrderStatus.PLACED) {

            log.warn(
                    "Order {} is not in PLACED status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setStatus(OrderStatus.PAYMENT_PROCESSING);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: PLACED -> PAYMENT_PROCESSING",
                order.getId()
        );
    }


    private void handlePaymentSuccess(PaymentSuccessEvent event) {

        log.info(
                "Received PaymentSuccessEvent: paymentId={}, orderId={}, userId={}, amount={}",
                event.getPaymentId(),
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount()
        );

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        if (order.getStatus() != OrderStatus.PAYMENT_PROCESSING) {

            log.warn(
                    "Order {} is not in PAYMENT_PROCESSING status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setPaymentSuccessful(true);

        if (order.isInventoryReserved()) {

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepo.save(order);

            log.info(
                    "Order {} payment successful and inventory already reserved. " +
                            "Status changed: PAYMENT_PROCESSING -> CONFIRMED",
                    order.getId()
            );

            OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getRestaurantId(),
                    order.getTotalAmount()
            );

            kafkaProducerService.publishOrderConfirmed(confirmedEvent);

        } else {

            orderRepo.save(order);

            log.info(
                    "Order {} payment successful. Waiting for inventory reservation.",
                    order.getId()
            );
        }
    }


    private void handlePaymentFailed(PaymentFailedEvent event) {

        log.info(
                "Received PaymentFailedEvent: orderId={}, userId={}, reason={}",
                event.getOrderId(),
                event.getUserId(),
                event.getReason()
        );

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        if (order.getStatus() != OrderStatus.PAYMENT_PROCESSING) {

            log.warn(
                    "Order {} is not in PAYMENT_PROCESSING status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: PAYMENT_PROCESSING -> PAYMENT_FAILED",
                order.getId()
        );
    }

    private void handleOrderAccepted(OrderAcceptedEvent event) {

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with id: " + event.getOrderId()
                ));

        if (order.getStatus() != OrderStatus.CONFIRMED) {

            log.warn(
                    "Order {} is not in CONFIRMED status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setStatus(OrderStatus.PREPARING);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: CONFIRMED -> PREPARING",
                order.getId()
        );
    }

    private void handleFoodReady(FoodReadyEvent event) {

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with id: " + event.getOrderId()
                ));

        if (order.getStatus() != OrderStatus.PREPARING) {

            log.warn(
                    "Order {} is not in PREPARING status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: PREPARING -> READY_FOR_PICKUP",
                order.getId()
        );
    }

    private void handleOutForDelivery(OutForDeliveryEvent event) {
        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {

            log.warn(
                    "Order {} is not in READY_FOR_PICKUP status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: READY_FOR_PICKUP -> OUT_FOR_DELIVERY",
                order.getId()
        );
    }

    private void handleDelivered(DeliveredEvent event) {

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {

            log.warn(
                    "Order {} is not in OUT_FOR_DELIVERY status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: OUT_FOR_DELIVERY -> DELIVERED",
                order.getId()
        );
    }

    private void handleInventoryReserved(InventoryReservedEvent event) {

        log.info(
                "Received InventoryReservedEvent: orderId={}",
                event.getOrderId()
        );

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        order.setInventoryReserved(true);

        if (order.isPaymentSuccessful()) {

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepo.save(order);

            log.info(
                    "Order {} inventory reserved and payment already successful. " +
                            "Status changed: PAYMENT_PROCESSING -> CONFIRMED",
                    order.getId()
            );

            OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                    order.getId(),
                    order.getUserId(),
                    order.getRestaurantId(),
                    order.getTotalAmount()
            );

            kafkaProducerService.publishOrderConfirmed(confirmedEvent);

        } else {

            orderRepo.save(order);

            log.info(
                    "Order {} inventory reserved. Waiting for payment success.",
                    order.getId()
            );
        }
    }

    private void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {

        log.info(
                "Handling inventory reservation failure: orderId={}, reason={}",
                event.getOrderId(),
                event.getReason()
        );

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        if (order.getStatus() != OrderStatus.PAYMENT_PROCESSING) {

            log.warn(
                    "Order {} is not in PAYMENT_PROCESSING status. Current status: {}",
                    order.getId(),
                    order.getStatus()
            );

            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: PAYMENT_PROCESSING -> CANCELLED " +
                        "due to inventory reservation failure",
                order.getId()
        );
    }

    private void handleInventoryReleased(InventoryReleasedEvent event) {

        log.info(
                "Handling inventory release: orderId={}",
                event.getOrderId()
        );

        Order order = orderRepo.findById(event.getOrderId())
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + event.getOrderId()
                        )
                );

        log.info(
                "Inventory reservations released for order {}",
                order.getId()
        );
    }
}