package com.foodflow.order_service.Kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodflow.order_service.Entity.Order;
import com.foodflow.order_service.Entity.OrderStatus;
import com.foodflow.common.Event.*;
import com.foodflow.order_service.Exception.OrderNotFoundException;
import com.foodflow.order_service.Repository.OrderRepo;
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

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);

        log.info(
                "Order {} status changed: PAYMENT_PROCESSING -> CONFIRMED",
                order.getId()
        );

        OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                order.getId(),
                order.getUserId(),
                order.getRestaurantId(),
                order.getTotalAmount()
        );

        kafkaProducerService.publishOrderConfirmed(confirmedEvent);

    }


    private void handlePaymentFailed(PaymentFailedEvent event) {

        log.info(
                "Received PaymentFailedEvent: orderId={}, reason={}",
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
}