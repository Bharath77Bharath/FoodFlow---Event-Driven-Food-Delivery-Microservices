package com.foodflow.order_service.Dto;

import com.foodflow.order_service.Entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDto {
    private Long orderId;
    private OrderStatus status;
}
