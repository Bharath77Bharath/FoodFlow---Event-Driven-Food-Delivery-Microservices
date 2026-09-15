package com.foodflow.inventory_service.Controller;

import com.foodflow.common.Event.OrderCreatedEvent;
import com.foodflow.inventory_service.Dto.CreateInventoryRequest;
import com.foodflow.inventory_service.Dto.InventoryAvailabilityRequest;
import com.foodflow.inventory_service.Dto.InventoryResponse;
import com.foodflow.inventory_service.Dto.UpdateInventoryRequest;
import com.foodflow.inventory_service.Service.InventoryService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = inventoryService.createInventory(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long inventoryId) {
        InventoryResponse response = inventoryService.getInventory(inventoryId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<InventoryResponse> getInventoryByMenuId(@PathVariable Long menuItemId) {
        InventoryResponse response = inventoryService.getInventoryByMenuItemId(menuItemId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        List<InventoryResponse> responseList = inventoryService.getAllInventory();

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @PutMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> updateInventory(@PathVariable Long inventoryId, @Valid @RequestBody UpdateInventoryRequest request) {
        InventoryResponse response = inventoryService.updateInventory(inventoryId, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-availability")
    public ResponseEntity<Void> checkAvailability(
            @RequestBody InventoryAvailabilityRequest request) {

        inventoryService.checkAvailability(request);

        return ResponseEntity.ok().build();
    }
}
