package com.foodflow.common.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutForDeliveryEvent {
    private Long deliveryId;
    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private Long deliveryPartnerId;
}
