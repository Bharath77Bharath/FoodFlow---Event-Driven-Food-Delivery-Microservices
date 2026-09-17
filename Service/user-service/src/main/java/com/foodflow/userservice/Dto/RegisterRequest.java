package com.foodflow.userservice.Dto;

import com.foodflow.userservice.Entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "[0-9]{10}$",
            message = "Phone number should contain exactly 10 digits"
    )
    private String phone;

    @NotNull(message = "Role is required")
    private UserRole userRole;

    @NotBlank(message = "Address is required")
    private String address;
}
