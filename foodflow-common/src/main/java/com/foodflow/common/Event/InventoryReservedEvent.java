package com.foodflow.common.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservedEvent {

    private Long orderId;
    private List<OrderItemEvent> items;

}
