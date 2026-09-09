package com.foodflow.delivery_service.Service;

import com.foodflow.delivery_service.Dto.DeliveryRequestDto;
import com.foodflow.delivery_service.Dto.DeliveryResponseDto;
import com.foodflow.delivery_service.Entity.Delivery;
import com.foodflow.delivery_service.Entity.DeliveryPartner;
import com.foodflow.delivery_service.Entity.DeliveryPartnerStatus;
import com.foodflow.delivery_service.Entity.DeliveryStatus;
import com.foodflow.delivery_service.Exception.*;
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
}
