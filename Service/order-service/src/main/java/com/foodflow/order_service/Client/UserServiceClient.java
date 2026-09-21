package com.foodflow.order_service.Client;

import com.foodflow.order_service.Config.InternalServiceFeignConfig;
import com.foodflow.order_service.Dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = InternalServiceFeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{id}")
    public UserResponseDto getUserById(@PathVariable Long id);
}
