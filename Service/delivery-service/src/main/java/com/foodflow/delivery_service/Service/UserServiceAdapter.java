package com.foodflow.delivery_service.Service;

import com.foodflow.delivery_service.Client.UserServiceClient;
import com.foodflow.delivery_service.Dto.UserResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceAdapter {

    private final UserServiceClient userServiceClient;

    @CircuitBreaker(
            name = "userService",
            fallbackMethod = "getUserFallback"
    )
    @Retry(name = "userService")
    public UserResponse getUserById(Long userId) {

        return userServiceClient.getUserById(userId);
    }

    private UserResponse getUserFallback(
            Long userId,
            Throwable throwable
    ) {

        if (throwable instanceof FeignException.NotFound) {
            throw new RuntimeException(
                    "User not found with id: " + userId
            );
        }

        throw new RuntimeException(
                "User Service is temporarily unavailable"
        );
    }
}