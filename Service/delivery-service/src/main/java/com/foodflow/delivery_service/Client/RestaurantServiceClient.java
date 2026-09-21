package com.foodflow.delivery_service.Client;

import com.foodflow.delivery_service.Config.InternalServiceFeignConfig;
import com.foodflow.delivery_service.Dto.RestaurantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service",configuration = InternalServiceFeignConfig.class)
public interface RestaurantServiceClient {

    @GetMapping("/api/v1/restaurants/{restaurantId}")
    public RestaurantResponse getRestaurantById(@PathVariable Long restaurantId);
}
