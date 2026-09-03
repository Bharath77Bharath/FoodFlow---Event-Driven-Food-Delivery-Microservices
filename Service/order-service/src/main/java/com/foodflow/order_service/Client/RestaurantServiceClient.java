package com.foodflow.order_service.Client;

import com.foodflow.order_service.Dto.MenuItemResponse;
import com.foodflow.order_service.Dto.RestaurantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantServiceClient {

    @GetMapping("/api/v1/restaurants/{id}")
    public RestaurantResponse getRestaurantById(@PathVariable Long id);

    @GetMapping("/api/v1/menu-items/{menuItemId}")
    public MenuItemResponse getMenuItemById(@PathVariable Long menuItemId);
}
