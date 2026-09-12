package com.foodflow.delivery_service.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {

    private Long id;
    private String address;
    private String city;

}
