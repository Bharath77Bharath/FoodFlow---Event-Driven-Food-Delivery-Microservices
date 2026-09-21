package com.foodflow.paymentservice.Client;


import com.foodflow.paymentservice.Dto.OrderResponseDto;
import com.foodflow.paymentservice.Security.InternalServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", configuration = InternalServiceFeignConfig.class)
public interface OrderServiceClient {
    @GetMapping("/api/v1/order/{orderId}")
    public OrderResponseDto getOrderById(@PathVariable Long orderId);
}
