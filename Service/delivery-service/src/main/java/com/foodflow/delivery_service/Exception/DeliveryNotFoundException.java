package com.foodflow.delivery_service.Exception;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(String message) {
        super(message);
    }
}
