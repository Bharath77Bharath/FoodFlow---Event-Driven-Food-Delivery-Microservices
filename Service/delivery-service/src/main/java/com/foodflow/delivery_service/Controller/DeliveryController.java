package com.foodflow.delivery_service.Controller;

import com.foodflow.delivery_service.Dto.DeliveryRequestDto;
import com.foodflow.delivery_service.Dto.DeliveryResponseDto;
import com.foodflow.delivery_service.Entity.DeliveryStatus;
import com.foodflow.delivery_service.Repository.DeliveryRepo;
import com.foodflow.delivery_service.Service.DeliveryService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponseDto> createDelivery(@Valid @RequestBody DeliveryRequestDto request) {
        DeliveryResponseDto response = deliveryService.createDelivery(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponseDto> getDeliveryById(@PathVariable Long deliveryId) {
        DeliveryResponseDto response = deliveryService.getDeliveryById(deliveryId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryResponseDto>> getAllDeliveries() {
        List<DeliveryResponseDto> response = deliveryService.getAllDeliveries();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponseDto> getDeliveryByOrderId(@PathVariable Long orderId) {
        DeliveryResponseDto response = deliveryService.getDeliveryByOrderId(orderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("{deliveryId}/status")
    public ResponseEntity<DeliveryResponseDto> updateDelivery(@PathVariable Long deliveryId, @RequestParam DeliveryStatus status) {
        DeliveryResponseDto response = deliveryService.updateDelivery(deliveryId,status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
