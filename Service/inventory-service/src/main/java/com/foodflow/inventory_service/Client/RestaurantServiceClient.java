package com.foodflow.inventory_service.Client;

import com.foodflow.inventory_service.Dto.MenuItemResponse;
import com.foodflow.inventory_service.Dto.RestaurantResponse;
import com.foodflow.inventory_service.Security.InternalServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service", configuration = InternalServiceFeignConfig.class)
public interface RestaurantServiceClient {
    @GetMapping("/api/v1/menu-items/{menuItemId}")
    MenuItemResponse getMenuItemById(@PathVariable Long menuItemId);

    @GetMapping("/api/v1/restaurants/{restaurantId}")
    RestaurantResponse getRestaurantById(@PathVariable Long restaurantId);
}
