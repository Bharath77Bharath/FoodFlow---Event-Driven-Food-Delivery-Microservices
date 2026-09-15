package com.foodflow.inventory_service.Dto;

import com.fasterxml.jackson.core.JsonToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
