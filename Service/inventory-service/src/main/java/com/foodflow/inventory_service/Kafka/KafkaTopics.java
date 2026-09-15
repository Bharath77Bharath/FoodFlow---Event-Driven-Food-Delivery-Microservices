package com.foodflow.inventory_service.Kafka;

public class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_EVENTS = "order-events";

    public static final String PAYMENT_EVENTS = "payment-events";

    public static final String INVENTORY_EVENTS = "inventory-events";

}