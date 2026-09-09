package com.foodflow.delivery_service.Exception;

public class DuplicateDeliveryException extends RuntimeException {
    public DuplicateDeliveryException(String message) {
        super(message);
    }
}
