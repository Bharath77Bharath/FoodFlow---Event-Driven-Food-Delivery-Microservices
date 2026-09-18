package com.foodflow.order_service.Security;

import com.foodflow.order_service.Client.RestaurantServiceClient;
import com.foodflow.order_service.Entity.Order;
import com.foodflow.order_service.Repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("orderAuthorization")
@RequiredArgsConstructor
public class OrderAuthorization {

    private final OrderRepo orderRepo;
    private final RestaurantServiceClient restaurantServiceClient;

    public boolean isCustomerOwner(Long orderId) {

        Order order = orderRepo.findById(orderId).orElse(null);

        if (order == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(order.getUserId());
    }

    public boolean isRestaurantOwner(Long orderId) {

        Order order = orderRepo.findById(orderId).orElse(null);

        if (order == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        try {

            var restaurant =
                    restaurantServiceClient.getRestaurantById(
                            order.getRestaurantId()
                    );

            return currentUserId.equals(restaurant.getOwnerId());

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCurrentUser(Long userId) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(userId);
    }
}