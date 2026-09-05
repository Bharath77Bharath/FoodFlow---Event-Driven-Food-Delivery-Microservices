package com.foodflow.order_service.Service;

import com.foodflow.order_service.Client.RestaurantServiceClient;
import com.foodflow.order_service.Client.UserServiceClient;
import com.foodflow.order_service.Dto.*;
import com.foodflow.order_service.Entity.Order;
import com.foodflow.order_service.Entity.OrderItem;
import com.foodflow.order_service.Entity.OrderStatus;
import com.foodflow.order_service.Exception.*;
import com.foodflow.order_service.Repository.OrderRepo;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final UserServiceClient userServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;

    public OrderResponse createOrder(CreateOrderRequest request) {

        UserResponse userResponse = getUser(request.getUserId());

        RestaurantResponse restaurantResponse = getRestaurant(request.getRestaurantId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for(OrderItemRequest itemRequest : request.getItems()) {
            MenuItemResponse menuItemResponse = getMenuItem(itemRequest.getMenuItemId());

            if(!menuItemResponse.getRestaurantId().equals(request.getRestaurantId())) {
                throw new InvalidOrderException("Menu item does not belong to the selected restaurant");
            }
            if(!menuItemResponse.getAvailable()) {
                throw new MenuItemUnavailableException("Menu item is not available");
            }

            BigDecimal price = BigDecimal.valueOf(menuItemResponse.getPrice());
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = new OrderItem();

            orderItem.setMenuItemId(menuItemResponse.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(price);
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setRestaurantId(request.getRestaurantId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        for(OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }
        order.setItems(orderItems);

        Order savedOrder = orderRepo.save(order);

        return convertOrderToOrderResponse(savedOrder);
    }

    //Helper Methods
    private OrderResponse convertOrderToOrderResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setId(order.getId());
        orderResponse.setUserId(order.getUserId());
        orderResponse.setRestaurantId(order.getRestaurantId());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for(OrderItem orderItem : order.getItems()) {
            OrderItemResponse item = new OrderItemResponse();

            item.setMenuItemId(orderItem.getMenuItemId());
            item.setQuantity(orderItem.getQuantity());
            item.setPrice(orderItem.getPrice());
            item.setSubtotal(orderItem.getSubtotal());

            itemResponses.add(item);
        }

        orderResponse.setItems(itemResponses);

        return orderResponse;
    }

    private UserResponse getUser(Long userId) {
        try {
            return userServiceClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found with id: " + userId);
        } catch (RetryableException e) {
            throw new ServiceUnavailableException("User service is currently unavailable");
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Unable to connect with user service");
        }
    }

    private RestaurantResponse getRestaurant(Long restaurantId) {
        try {
            return restaurantServiceClient.getRestaurantById(restaurantId);
        } catch (FeignException.NotFound e) {
            throw new RestaurantNotFoundException("Restaurant not found with id: " + restaurantId);
        } catch (RetryableException e) {
            throw new ServiceUnavailableException("Restaurant service is currently unavailable");
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Unable to connect with restaurant service");
        }
    }

    private MenuItemResponse getMenuItem(Long menuItemId) {
        try {
            return restaurantServiceClient.getMenuItemById(menuItemId);
        } catch (FeignException.NotFound e) {
            throw new MenuItemNotFoundException("MenuItem not found with id: " + menuItemId);
        } catch (RetryableException e) {
            throw new ServiceUnavailableException("Restaurant service is currently unavailable");
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Unable to connect with restaurant service");
        }
    }
}
