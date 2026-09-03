package com.foodflow.order_service.Service;

import com.foodflow.order_service.Client.RestaurantServiceClient;
import com.foodflow.order_service.Client.UserServiceClient;
import com.foodflow.order_service.Dto.*;
import com.foodflow.order_service.Entity.Order;
import com.foodflow.order_service.Entity.OrderItem;
import com.foodflow.order_service.Entity.OrderStatus;
import com.foodflow.order_service.Repository.OrderRepo;
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

    public OrderResponse createOrder(CreateOrderRequest request) {

        UserResponse userResponse = userServiceClient.getUserById(request.getUserId());

        RestaurantResponse restaurantResponse = restaurantServiceClient.getRestaurantById(request.getRestaurantId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for(OrderItemRequest itemRequest : request.getItems()) {
            MenuItemResponse menuItemResponse = restaurantServiceClient.getMenuItemById(itemRequest.getMenuItemId());

            if(!menuItemResponse.getRestaurantId().equals(request.getRestaurantId())) {
                throw new RuntimeException("Menu item does not belong to the selected restaurant");
            }
            if(!menuItemResponse.getAvailable()) {
                throw new RuntimeException("Menu item is not available");
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
}
