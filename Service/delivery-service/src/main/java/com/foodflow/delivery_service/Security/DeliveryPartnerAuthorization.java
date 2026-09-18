package com.foodflow.delivery_service.Security;

import com.foodflow.delivery_service.Entity.DeliveryPartner;
import com.foodflow.delivery_service.Repository.DeliveryPartnerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("deliveryPartnerAuthorization")
@RequiredArgsConstructor
public class DeliveryPartnerAuthorization {

    private final DeliveryPartnerRepo deliveryPartnerRepo;

    public boolean isOwner(Long partnerId) {

        DeliveryPartner partner =
                deliveryPartnerRepo.findById(partnerId).orElse(null);

        if (partner == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(partner.getUserId());
    }
}