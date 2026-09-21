package com.foodflow.paymentservice.Service;

import com.foodflow.paymentservice.Client.OrderServiceClient;
import com.foodflow.paymentservice.Dto.OrderResponseDto;
import com.foodflow.paymentservice.Exception.OrderNotFoundException;
import com.foodflow.paymentservice.Exception.OrderServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceAdapter {

    private final OrderServiceClient orderServiceClient;

    @CircuitBreaker(
            name = "orderService",
            fallbackMethod = "getOrderFallback"
    )
    @Retry(name = "orderService")
    public OrderResponseDto getOrderById(Long orderId) {
        return orderServiceClient.getOrderById(orderId);
    }

    private OrderResponseDto getOrderFallback(
            Long orderId,
            Throwable throwable
    ) {

        if (throwable instanceof FeignException.NotFound) {
            throw new OrderNotFoundException(
                    "Order not found with id: " + orderId
            );
        }

        throw new OrderServiceUnavailableException(
                "Order Service is temporarily unavailable"
        );
    }
}