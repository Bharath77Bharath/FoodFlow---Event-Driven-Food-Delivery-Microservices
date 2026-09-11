package com.foodflow.order_service.Entity;

public enum OrderStatus {
    PLACED,
    PAYMENT_PROCESSING,
    CONFIRMED,
    PAYMENT_FAILED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
