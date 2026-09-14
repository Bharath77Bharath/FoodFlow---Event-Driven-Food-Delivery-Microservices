package com.foodflow.restaurantservice.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantRequest {

    @NotNull(message = "Owner id is required")
    private Long ownerId;

    @NotBlank(message = "Restaurant Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "[0-9]{10}$",
            message = "Phone number should contain exactly 10 digits"
    )
    private String phone;

    @Email(message = "Invalid Email format")
    private String email;
}
