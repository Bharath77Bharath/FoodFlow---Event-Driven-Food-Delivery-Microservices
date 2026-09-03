package com.foodflow.order_service.Controller;

import com.foodflow.order_service.Client.RestaurantServiceClient;
import com.foodflow.order_service.Dto.MenuItemResponse;
import com.foodflow.order_service.Dto.RestaurantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/restaurant")
public class TestController {

    private final RestaurantServiceClient restaurantServiceClient;

    @GetMapping("/{id}")
    public RestaurantResponse getRestaurant(
            @PathVariable("id") Long id) {

        return restaurantServiceClient.getRestaurantById(id);
    }

    @GetMapping("/menu-item/{id}")
    public MenuItemResponse getMenuItem(
            @PathVariable("id") Long id) {

        return restaurantServiceClient.getMenuItemById(id);
    }
}