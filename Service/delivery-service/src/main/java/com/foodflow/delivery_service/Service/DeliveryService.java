package com.foodflow.delivery_service.Service;

import com.foodflow.common.Event.DeliveryAssignedEvent;
import com.foodflow.delivery_service.Client.RestaurantServiceClient;
import com.foodflow.delivery_service.Client.UserServiceClient;
import com.foodflow.delivery_service.Dto.DeliveryRequestDto;
import com.foodflow.delivery_service.Dto.DeliveryResponseDto;
import com.foodflow.delivery_service.Dto.RestaurantResponse;
import com.foodflow.delivery_service.Dto.UserResponse;
import com.foodflow.delivery_service.Entity.Delivery;
import com.foodflow.delivery_service.Entity.DeliveryPartner;
import com.foodflow.delivery_service.Entity.DeliveryPartnerStatus;
import com.foodflow.delivery_service.Entity.DeliveryStatus;
import com.foodflow.delivery_service.Exception.*;
import com.foodflow.delivery_service.Kafka.DeliveryKafkaProducer;
import com.foodflow.delivery_service.Repository.DeliveryPartnerRepo;
import com.foodflow.delivery_service.Repository.DeliveryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepo deliveryRepo;
    private final DeliveryPartnerRepo deliveryPartnerRepo;
    private final UserServiceClient userServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;
    private final DeliveryKafkaProducer deliveryKafkaProducer;
    private final DeliverySimulationService deliverySimulationService;

    @Transactional
    public DeliveryResponseDto createDelivery(DeliveryRequestDto request) {
        if(deliveryRepo.existsByOrderId(request.getOrderId())) {
            throw new DuplicateDeliveryException("Delivery already exists for order id: "+request.getOrderId());
        }

        DeliveryPartner partner = deliveryPartnerRepo.findById(request.getDeliveryPartnerId()).orElseThrow(() -> new DeliveryPartnerNotFoundException("Delivery partner not found with id: "+request.getDeliveryPartnerId()));

        if(partner.getStatus() != DeliveryPartnerStatus.AVAILABLE) {
            throw new NoAvailablePartnerException("Delivery partner is not available");
        }

        Delivery delivery = convertToDelivery(request);

        Delivery savedDelivery = deliveryRepo.save(delivery);

        partner.setStatus(DeliveryPartnerStatus.BUSY);
        deliveryPartnerRepo.save(partner);

        return convertToDeliveryResponse(savedDelivery);
    }

    public List<DeliveryResponseDto> getAllDeliveries() {

        return deliveryRepo.findAll().
                stream().
                map(this::convertToDeliveryResponse).
                toList();

    }

    public DeliveryResponseDto getDeliveryById(Long deliveryId) {
        Delivery delivery = deliveryRepo.findById(deliveryId).orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with delivery id: "+deliveryId));

        return convertToDeliveryResponse(delivery);
    }

    public DeliveryResponseDto getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepo.findByOrderId(orderId).orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with order id: "+orderId));

        return convertToDeliveryResponse(delivery);
    }

    @Transactional
    public DeliveryResponseDto updateDelivery(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepo.findById(deliveryId).orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with id: "+deliveryId));

        DeliveryStatus currentStatus = delivery.getStatus();

        if(!isValidStatusTransition(currentStatus, status)) {
            throw new InvalidDeliveryStatusException("Invalid delivery status transition from "+currentStatus+" to "+status);
        }

        delivery.setStatus(status);
        Delivery updatedDelivery = deliveryRepo.save(delivery);

        if(status == DeliveryStatus.DELIVERED || status == DeliveryStatus.CANCELLED) {
            DeliveryPartner partner = deliveryPartnerRepo.findById(delivery.getDeliveryPartnerId()).orElseThrow(() -> new DeliveryPartnerNotFoundException("Delivery partner not found with id: "+delivery.getDeliveryPartnerId()));

            partner.setStatus(DeliveryPartnerStatus.AVAILABLE);
            deliveryPartnerRepo.save(partner);
        }

        return convertToDeliveryResponse(updatedDelivery);

    }

    public DeliveryResponseDto createDeliveryFromFoodReady(Long orderId, Long restaurantId, Long userId) {

        if(deliveryRepo.existsByOrderId(orderId)) {
            throw new DuplicateDeliveryException("Delivery already exists for order id: "+orderId);
        }

        String deliveryAddress = getCustomerAddress(userId);
        String pickupAddress = getRestaurantAddress(restaurantId);
        DeliveryPartner partner = findAvailablePartner();

        Delivery delivery = new Delivery();

        delivery.setOrderId(orderId);
        delivery.setUserId(userId);
        delivery.setRestaurantId(restaurantId);
        delivery.setDeliveryPartnerId(partner.getId());
        delivery.setDeliveryAddress(deliveryAddress);
        delivery.setPickupAddress(pickupAddress);

        Delivery savedDelivery = deliveryRepo.save(delivery);

        DeliveryAssignedEvent event = new DeliveryAssignedEvent(
                savedDelivery.getId(),
                savedDelivery.getOrderId(),
                savedDelivery.getUserId(),
                savedDelivery.getRestaurantId(),
                savedDelivery.getDeliveryPartnerId()

        );

        deliveryKafkaProducer.publishDeliveryAssigned(event);

        partner.setStatus(DeliveryPartnerStatus.BUSY);
        partner.setAvailableSince(null);

        deliveryPartnerRepo.save(partner);

        deliverySimulationService.simulateDelivery(event.getDeliveryId());

        return convertToDeliveryResponse(savedDelivery);
    }

    //helper methods

    private Delivery convertToDelivery(DeliveryRequestDto request) {
        Delivery delivery = new Delivery();

        delivery.setOrderId(request.getOrderId());
        delivery.setDeliveryPartnerId(request.getDeliveryPartnerId());
        delivery.setPickupAddress(request.getPickupAddress());
        delivery.setDeliveryAddress(request.getDeliveryAddress());

        return delivery;
    }

    private DeliveryResponseDto convertToDeliveryResponse(Delivery delivery) {
        DeliveryResponseDto response = new DeliveryResponseDto();

        response.setId(delivery.getId());
        response.setOrderId(delivery.getOrderId());
        response.setDeliveryPartnerId(delivery.getDeliveryPartnerId());
        response.setPickupAddress(delivery.getPickupAddress());
        response.setDeliveryAddress(delivery.getDeliveryAddress());
        response.setStatus(delivery.getStatus());
        response.setCreatedAt(delivery.getCreatedAt());
        response.setUpdatedAt(delivery.getUpdatedAt());

        return response;
    }

    private boolean isValidStatusTransition(DeliveryStatus currentStatus, DeliveryStatus newStatus) {
        if(currentStatus == DeliveryStatus.ASSIGNED) {
            return newStatus == DeliveryStatus.PICKED_UP || newStatus == DeliveryStatus.CANCELLED;
        }
        else if(currentStatus == DeliveryStatus.PICKED_UP) {
            return newStatus == DeliveryStatus.OUT_FOR_DELIVERY;
        }
        else if(currentStatus == DeliveryStatus.OUT_FOR_DELIVERY) {
            return newStatus == DeliveryStatus.DELIVERED;
        }

        return false;
    }

    private DeliveryPartner findAvailablePartner() {

        return deliveryPartnerRepo
                .findFirstByStatusOrderByAvailableSinceAsc(
                        DeliveryPartnerStatus.AVAILABLE
                )
                .orElseThrow(() ->
                        new NoAvailablePartnerException(
                                "No delivery partner is currently available"
                        )
                );
    }

    private String getCustomerAddress(Long userId) {
        UserResponse response = userServiceClient.getUserById(userId);

        return response.getAddress();
    }

    private String getRestaurantAddress(Long restaurantId) {
        RestaurantResponse response = restaurantServiceClient.getRestaurantById(restaurantId);

        return response.getAddress();
    }
}
