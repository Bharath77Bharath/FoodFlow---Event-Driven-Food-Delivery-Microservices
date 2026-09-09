package com.foodflow.delivery_service.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRequestDto {

    @NotNull(message = "Order Id is required")
    private Long orderId;

    @NotNull(message = "Delivery partner Id is required")
    private Long deliveryPartnerId;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
}
