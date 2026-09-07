package com.foodflow.order_service.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
