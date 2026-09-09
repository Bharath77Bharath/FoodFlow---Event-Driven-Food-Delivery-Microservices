package com.foodflow.delivery_service.Dto;

import com.foodflow.delivery_service.Entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponseDto {
    private Long id;
    private Long orderId;
    private Long deliveryPartnerId;
    private String pickupAddress;
    private String deliveryAddress;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
