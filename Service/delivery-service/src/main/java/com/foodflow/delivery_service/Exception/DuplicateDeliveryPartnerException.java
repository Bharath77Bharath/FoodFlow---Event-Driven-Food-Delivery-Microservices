package com.foodflow.delivery_service.Exception;

public class DuplicateDeliveryPartnerException extends RuntimeException {
    public DuplicateDeliveryPartnerException(String message) {
        super(message);
    }
}
