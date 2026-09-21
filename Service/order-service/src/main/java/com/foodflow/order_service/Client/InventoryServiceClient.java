package com.foodflow.order_service.Client;

import com.foodflow.common.Event.OrderCreatedEvent;
import com.foodflow.order_service.Config.InternalServiceFeignConfig;
import com.foodflow.order_service.Dto.InventoryAvailabilityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", configuration = InternalServiceFeignConfig.class)
public interface InventoryServiceClient {

    @PostMapping("/api/v1/inventory/check-availability")
    public void checkAvailability(@RequestBody InventoryAvailabilityRequest request);
}
