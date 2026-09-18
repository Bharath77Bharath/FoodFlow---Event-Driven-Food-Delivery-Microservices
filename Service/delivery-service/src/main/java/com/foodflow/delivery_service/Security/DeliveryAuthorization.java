package com.foodflow.delivery_service.Security;

import com.foodflow.delivery_service.Entity.Delivery;
import com.foodflow.delivery_service.Entity.DeliveryPartner;
import com.foodflow.delivery_service.Repository.DeliveryPartnerRepo;
import com.foodflow.delivery_service.Repository.DeliveryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("deliveryAuthorization")
@RequiredArgsConstructor
public class DeliveryAuthorization {

    private final DeliveryRepo deliveryRepo;
    private final DeliveryPartnerRepo deliveryPartnerRepo;

    public boolean isCustomerOwner(Long deliveryId) {

        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElse(null);

        if (delivery == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(delivery.getUserId());
    }

    public boolean isAssignedDeliveryPartner(Long deliveryId) {

        Delivery delivery = deliveryRepo.findById(deliveryId)
                .orElse(null);

        if (delivery == null || delivery.getDeliveryPartnerId() == null) {
            return false;
        }

        DeliveryPartner partner = deliveryPartnerRepo
                .findById(delivery.getDeliveryPartnerId())
                .orElse(null);

        if (partner == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(partner.getUserId());
    }

    public boolean isCustomerOwnerByOrderId(Long orderId) {

        Delivery delivery = deliveryRepo.findByOrderId(orderId)
                .orElse(null);

        if (delivery == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(delivery.getUserId());
    }

    public boolean isAssignedPartnerByOrderId(Long orderId) {

        Delivery delivery = deliveryRepo.findByOrderId(orderId)
                .orElse(null);

        if (delivery == null || delivery.getDeliveryPartnerId() == null) {
            return false;
        }

        DeliveryPartner partner = deliveryPartnerRepo
                .findById(delivery.getDeliveryPartnerId())
                .orElse(null);

        if (partner == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(partner.getUserId());
    }
}