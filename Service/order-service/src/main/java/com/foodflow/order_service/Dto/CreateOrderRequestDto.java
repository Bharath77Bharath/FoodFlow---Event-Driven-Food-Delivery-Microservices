package com.foodflow.order_service.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequestDto {
    private Long userId;
    private Long restaurantId;
    private List<OrderItemRequestDto> items;
}
