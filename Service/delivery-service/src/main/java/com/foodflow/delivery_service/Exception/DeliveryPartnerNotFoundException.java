package com.foodflow.delivery_service.Exception;

public class DeliveryPartnerNotFoundException extends RuntimeException {
    public DeliveryPartnerNotFoundException(String message) {
        super(message);
    }
}
