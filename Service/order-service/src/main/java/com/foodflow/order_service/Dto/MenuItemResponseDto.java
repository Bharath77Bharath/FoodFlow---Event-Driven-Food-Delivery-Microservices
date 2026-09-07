package com.foodflow.order_service.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemResponseDto {

    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private Double price;
    private Boolean available;

}
