package com.foodflow.order_service.Controller;

import com.foodflow.order_service.Dto.CreateOrderRequestDto;
import com.foodflow.order_service.Dto.OrderResponseDto;
import com.foodflow.order_service.Dto.OrderStatusDto;
import com.foodflow.order_service.Dto.UpdateOrderStatusDto;
import com.foodflow.order_service.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody CreateOrderRequestDto request) {
        OrderResponseDto response = orderService.createOrder(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getOrderByUser(@PathVariable Long userId) {
        List<OrderResponseDto> responses = orderService.getOrderByUser(userId);

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatusDto> getOrderStatus(@PathVariable Long orderId) {
        OrderStatusDto statusDto = orderService.getOrderStatus(orderId);

        return new ResponseEntity<>(statusDto, HttpStatus.OK);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateOrderStatusDto updateOrderStatusDto) {
        OrderResponseDto responseDto = orderService.updateOrderStatus(orderId,updateOrderStatusDto);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
