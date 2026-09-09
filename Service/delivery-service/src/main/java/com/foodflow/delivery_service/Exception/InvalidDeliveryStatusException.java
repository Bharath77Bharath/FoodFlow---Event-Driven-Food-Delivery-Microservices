package com.foodflow.delivery_service.Exception;

public class InvalidDeliveryStatusException extends RuntimeException {
    public InvalidDeliveryStatusException(String message) {
        super(message);
    }
}
