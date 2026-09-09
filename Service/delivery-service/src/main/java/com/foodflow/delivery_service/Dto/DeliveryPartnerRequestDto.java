package com.foodflow.delivery_service.Dto;

import com.foodflow.delivery_service.Entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPartnerRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "[0-9]{10}$",
            message = "Phone number should contain only 10 digits"
    )
    private String phone;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;
}
