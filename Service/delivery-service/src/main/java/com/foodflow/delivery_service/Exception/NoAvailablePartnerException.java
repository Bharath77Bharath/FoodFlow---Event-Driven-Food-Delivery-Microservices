package com.foodflow.delivery_service.Exception;

public class NoAvailablePartnerException extends RuntimeException {
    public NoAvailablePartnerException(String message) {
        super(message);
    }
}
