package com.foodflow.paymentservice.Client;


import com.foodflow.paymentservice.Dto.OrderResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service")
public interface OrderServiceClient {
    @GetMapping("/api/v1/order/{orderId}")
    public OrderResponseDto getOrderById(@PathVariable Long orderId);
}
