package com.foodflow.common.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveredEvent {

    private Long deliveryId;
    private Long orderId;
    private Long deliveryPartnerId;
}