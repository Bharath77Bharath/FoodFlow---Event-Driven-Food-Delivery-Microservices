package com.foodflow.order_service.Exception;

public class MenuItemUnavailableException extends RuntimeException {
    public MenuItemUnavailableException(String message) {
        super(message);
    }
}
