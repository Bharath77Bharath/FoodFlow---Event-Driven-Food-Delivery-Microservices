package com.foodflow.delivery_service.Client;

import com.foodflow.delivery_service.Config.InternalServiceFeignConfig;
import com.foodflow.delivery_service.Dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = InternalServiceFeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    public UserResponse getUserById(@PathVariable Long userId);
}
