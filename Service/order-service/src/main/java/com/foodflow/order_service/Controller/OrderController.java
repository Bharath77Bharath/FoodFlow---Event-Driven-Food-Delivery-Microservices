package com.foodflow.order_service.Controller;

import com.foodflow.order_service.Dto.CreateOrderRequestDto;
import com.foodflow.order_service.Dto.OrderResponseDto;
import com.foodflow.order_service.Dto.OrderStatusDto;
import com.foodflow.order_service.Dto.UpdateOrderStatusDto;
import com.foodflow.order_service.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Valid CreateOrderRequestDto request) {
        OrderResponseDto response = orderService.createOrder(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@orderAuthorization.isCustomerOwner(#orderId) or " +
                    "@orderAuthorization.isRestaurantOwner(#orderId)"
    )
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "(hasRole('CUSTOMER') and @orderAuthorization.isCurrentUser(#userId))"
    )
    public ResponseEntity<List<OrderResponseDto>> getOrderByUser(@PathVariable Long userId) {
        List<OrderResponseDto> responses = orderService.getOrderByUser(userId);

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{orderId}/status")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@orderAuthorization.isCustomerOwner(#orderId) or " +
                    "@orderAuthorization.isRestaurantOwner(#orderId)"
    )
    public ResponseEntity<OrderStatusDto> getOrderStatus(@PathVariable Long orderId) {
        OrderStatusDto statusDto = orderService.getOrderStatus(orderId);

        return new ResponseEntity<>(statusDto, HttpStatus.OK);
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateOrderStatusDto updateOrderStatusDto) {
        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId,updateOrderStatusDto);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
