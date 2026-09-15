package com.foodflow.inventory_service.Client;

import com.foodflow.inventory_service.Dto.MenuItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantServiceClient {
    @GetMapping("/api/v1/menu-items/{menuItemId}")
    public MenuItemResponse getMenuItemById(@PathVariable Long menuItemId);
}
