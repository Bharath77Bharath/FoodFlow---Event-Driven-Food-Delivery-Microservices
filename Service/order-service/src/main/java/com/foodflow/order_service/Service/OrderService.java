package com.foodflow.order_service.Service;

import com.foodflow.order_service.Client.RestaurantServiceClient;
import com.foodflow.order_service.Client.UserServiceClient;
import com.foodflow.order_service.Dto.*;
import com.foodflow.order_service.Entity.Order;
import com.foodflow.order_service.Entity.OrderItem;
import com.foodflow.order_service.Entity.OrderStatus;
import com.foodflow.common.Event.OrderCreatedEvent;
import com.foodflow.order_service.Exception.*;
import com.foodflow.order_service.Kafka.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducerService;

    public OrderResponseDto createOrder(CreateOrderRequestDto request) {

        UserResponseDto userResponse = getUser(request.getUserId());

        RestaurantResponseDto restaurantResponse = getRestaurant(request.getRestaurantId());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for(OrderItemRequestDto itemRequest : request.getItems()) {
            MenuItemResponseDto menuItemResponse = getMenuItem(itemRequest.getMenuItemId());

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

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getRestaurantId(),
                savedOrder.getTotalAmount()
        );

        kafkaProducerService.publishOrderCreated(event);

        return convertOrderToOrderResponse(savedOrder);
    }

    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found with id: "+orderId));

        return convertOrderToOrderResponse(order);
    }

    public List<OrderResponseDto> getOrderByUser(Long userId) {
        getUser(userId);
        List<Order> orderList = orderRepo.findByUserId(userId);
        List<OrderResponseDto> orderResponseList = new ArrayList<>();
        for(Order order : orderList) {
            orderResponseList.add(convertOrderToOrderResponse(order));
        }

        return orderResponseList;
    }

    public OrderStatusDto getOrderStatus(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found with id: "+orderId));

        return new OrderStatusDto(order.getId(),order.getStatus());
    }

    public OrderResponseDto updateOrderStatus(Long orderId, UpdateOrderStatusDto responseDto) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found with id: "+orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = responseDto.getStatus();

        if(!isValidOrderStatusTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStatusException(
                    "Cannot change order status from "+currentStatus+" to "+newStatus
            );
        }

        order.setStatus(newStatus);

        Order savedOrder = orderRepo.save(order);

        return convertOrderToOrderResponse(savedOrder);
    }

    //Helper Methods
    private OrderResponseDto convertOrderToOrderResponse(Order order) {
        OrderResponseDto orderResponse = new OrderResponseDto();

        orderResponse.setId(order.getId());
        orderResponse.setUserId(order.getUserId());
        orderResponse.setRestaurantId(order.getRestaurantId());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponseDto> itemResponses = new ArrayList<>();

        for(OrderItem orderItem : order.getItems()) {
            OrderItemResponseDto item = new OrderItemResponseDto();

            item.setMenuItemId(orderItem.getMenuItemId());
            item.setQuantity(orderItem.getQuantity());
            item.setPrice(orderItem.getPrice());
            item.setSubtotal(orderItem.getSubtotal());

            itemResponses.add(item);
        }

        orderResponse.setItems(itemResponses);

        return orderResponse;
    }

    private UserResponseDto getUser(Long userId) {
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

    private RestaurantResponseDto getRestaurant(Long restaurantId) {
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

    private MenuItemResponseDto getMenuItem(Long menuItemId) {
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

    private boolean isValidOrderStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if(currentStatus == OrderStatus.PLACED) {
            return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
        }
        if(currentStatus == OrderStatus.CONFIRMED) {
            return newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.CANCELLED;
        }
        if(currentStatus == OrderStatus.PREPARING) {
            return newStatus == OrderStatus.OUT_FOR_DELIVERY;
        }
        if(currentStatus == OrderStatus.OUT_FOR_DELIVERY) {
            return newStatus == OrderStatus.DELIVERED;
        }

        return false;
    }
}
