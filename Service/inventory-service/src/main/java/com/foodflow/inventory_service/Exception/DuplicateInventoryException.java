package com.foodflow.inventory_service.Exception;

public class DuplicateInventoryException extends RuntimeException {
    public DuplicateInventoryException(String message) {
        super(message);
    }
}
