package com.foodflow.delivery_service.Dto;

import com.foodflow.delivery_service.Entity.DeliveryPartnerStatus;
import com.foodflow.delivery_service.Entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPartnerResponseDto {
    private Long id;
    private String name;
    private String phone;
    private VehicleType vehicleType;
    private String vehicleNumber;
    private DeliveryPartnerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
