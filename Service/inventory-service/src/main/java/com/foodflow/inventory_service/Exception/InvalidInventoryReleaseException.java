package com.foodflow.inventory_service.Exception;

public class InvalidInventoryReleaseException extends RuntimeException {
    public InvalidInventoryReleaseException(String message) {
        super(message);
    }
}
