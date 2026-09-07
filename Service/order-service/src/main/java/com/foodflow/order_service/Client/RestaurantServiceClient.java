package com.foodflow.order_service.Client;

import com.foodflow.order_service.Dto.MenuItemResponseDto;
import com.foodflow.order_service.Dto.RestaurantResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantServiceClient {

    @GetMapping("/api/v1/restaurants/{id}")
    public RestaurantResponseDto getRestaurantById(@PathVariable Long id);

    @GetMapping("/api/v1/menu-items/{menuItemId}")
    public MenuItemResponseDto getMenuItemById(@PathVariable Long menuItemId);
}
