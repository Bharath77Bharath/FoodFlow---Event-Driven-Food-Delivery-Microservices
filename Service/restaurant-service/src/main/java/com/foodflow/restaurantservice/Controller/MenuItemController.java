package com.foodflow.restaurantservice.Controller;

import com.foodflow.restaurantservice.Dto.MenuItemRequest;
import com.foodflow.restaurantservice.Dto.MenuItemResponse;
import com.foodflow.restaurantservice.Entity.MenuItem;
import com.foodflow.restaurantservice.Service.MenuItemService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    @PreAuthorize("hasRole('ADMIN') or @restaurantAuthorization.isOwner(#restaurantId)")
    public ResponseEntity<MenuItemResponse> createMenuItem(@PathVariable Long restaurantId,@RequestBody MenuItemRequest request) {
        MenuItemResponse response = menuItemService.createMenuItem(restaurantId,request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemByRestaurant(@PathVariable Long restaurantId) {
        List<MenuItemResponse> responseList = menuItemService.getMenuItemByRestaurant(restaurantId);

        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @GetMapping("/menu-items/{menuItemId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long menuItemId) {
        MenuItemResponse response = menuItemService.getMenuItemById(menuItemId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/menu-items/{menuItemId}")
    @PreAuthorize("hasRole('ADMIN') or @restaurantAuthorization.ownsMenuItem(#menuItemId)")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long menuItemId, @RequestBody MenuItemRequest request) {
        MenuItemResponse response = menuItemService.updateMenuItem(menuItemId,request);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @DeleteMapping("/menu-items/{menuItemId}")
    @PreAuthorize("hasRole('ADMIN') or @restaurantAuthorization.ownsMenuItem(#menuItemId)")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long menuItemId) {
        menuItemService.deleteMenuItem(menuItemId);

        return ResponseEntity.noContent().build();
    }
}
