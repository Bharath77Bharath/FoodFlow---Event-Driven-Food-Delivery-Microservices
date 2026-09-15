package com.foodflow.order_service.Dto;

import com.foodflow.common.Event.OrderItemEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAvailabilityRequest {

    private List<OrderItemEvent> items;
}