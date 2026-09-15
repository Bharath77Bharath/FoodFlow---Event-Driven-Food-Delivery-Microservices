package com.foodflow.delivery_service.Service;

import com.foodflow.common.Event.*;
import com.foodflow.delivery_service.Entity.Delivery;
import com.foodflow.delivery_service.Entity.DeliveryPartner;
import com.foodflow.delivery_service.Entity.DeliveryPartnerStatus;
import com.foodflow.delivery_service.Entity.DeliveryStatus;
import com.foodflow.delivery_service.Exception.DeliveryNotFoundException;
import com.foodflow.delivery_service.Kafka.DeliveryKafkaProducer;
import com.foodflow.delivery_service.Repository.DeliveryPartnerRepo;
import com.foodflow.delivery_service.Repository.DeliveryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliverySimulationService {

    private final DeliveryRepo deliveryRepo;
    private final DeliveryKafkaProducer deliveryKafkaProducer;
    private final DeliveryPartnerRepo deliveryPartnerRepo;

    @Async
    public void simulateDelivery(Long deliveryId) {

        try {

            // ASSIGNED → PICKED_UP
            log.info("Delivery {} waiting 10 seconds before PICKED_UP", deliveryId);
            Thread.sleep(10_000);

            Delivery delivery = deliveryRepo.findById(deliveryId)
                    .orElseThrow(() ->
                            new DeliveryNotFoundException(
                                    "Delivery not found with id: " + deliveryId
                            )
                    );

            delivery.setStatus(DeliveryStatus.PICKED_UP);
            deliveryRepo.save(delivery);

            log.info(
                    "Delivery {} status changed: ASSIGNED -> PICKED_UP",
                    deliveryId
            );

            // PICKED_UP → OUT_FOR_DELIVERY
            log.info("Delivery {} waiting 10 seconds before OUT_FOR_DELIVERY", deliveryId);
            Thread.sleep(10_000);

            delivery = deliveryRepo.findById(deliveryId)
                    .orElseThrow(() ->
                            new DeliveryNotFoundException(
                                    "Delivery not found with id: " + deliveryId
                            )
                    );

            delivery.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);
            deliveryRepo.save(delivery);

            log.info(
                    "Delivery {} status changed: PICKED_UP -> OUT_FOR_DELIVERY",
                    deliveryId
            );

            OutForDeliveryEvent event =
                    new OutForDeliveryEvent(
                            delivery.getId(),
                            delivery.getOrderId(),
                            delivery.getUserId(),
                            delivery.getRestaurantId(),
                            delivery.getDeliveryPartnerId()
                    );

            deliveryKafkaProducer.publishOutForDelivery(event);

            // OUT_FOR_DELIVERY → DELIVERED
            log.info("Delivery {} waiting 10 seconds before DELIVERED", deliveryId);
            Thread.sleep(10_000);

            delivery = deliveryRepo.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException(
                            "Delivery not found with id: " + deliveryId
                    ));

            delivery.setStatus(DeliveryStatus.DELIVERED);
            deliveryRepo.save(delivery);

            log.info(
                    "Delivery {} status changed: OUT_FOR_DELIVERY -> DELIVERED",
                    deliveryId
            );

            DeliveredEvent deliveredEvent =
                    new DeliveredEvent(
                            delivery.getId(),
                            delivery.getOrderId(),
                            delivery.getUserId(),
                            delivery.getRestaurantId(),
                            delivery.getDeliveryPartnerId()
                    );

            deliveryKafkaProducer.publishDelivered(deliveredEvent);

            log.info(
                    "DeliveredEvent published for order {}",
                    delivery.getOrderId()
            );

            Delivery finalDelivery = delivery;
            DeliveryPartner partner =
                    deliveryPartnerRepo.findById(delivery.getDeliveryPartnerId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Delivery partner not found with id: "
                                            + finalDelivery.getDeliveryPartnerId()
                            ));

            partner.setStatus(DeliveryPartnerStatus.AVAILABLE);
            partner.setAvailableSince(LocalDateTime.now());

            deliveryPartnerRepo.save(partner);

            PartnerAvailableEvent partnerAvailableEvent =
                    new PartnerAvailableEvent(partner.getId());

            deliveryKafkaProducer.publishPartnerAvailable(partnerAvailableEvent);

            log.info(
                    "Delivery partner {} is AVAILABLE again and added to FIFO queue",
                    partner.getId()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            log.error(
                    "Delivery simulation interrupted for delivery {}",
                    deliveryId,
                    e
            );
        }
    }
}
