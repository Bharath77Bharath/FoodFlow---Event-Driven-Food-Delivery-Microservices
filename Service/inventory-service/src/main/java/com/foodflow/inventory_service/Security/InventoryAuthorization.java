package com.foodflow.inventory_service.Security;

import com.foodflow.inventory_service.Client.RestaurantServiceClient;
import com.foodflow.inventory_service.Entity.Inventory;
import com.foodflow.inventory_service.Repository.InventoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("inventoryAuthorization")
@RequiredArgsConstructor
public class InventoryAuthorization {

    private final InventoryRepo inventoryRepo;
    private final RestaurantServiceClient restaurantServiceClient;

    public boolean isOwner(Long inventoryId) {

        Inventory inventory = inventoryRepo.findById(inventoryId)
                .orElse(null);

        if (inventory == null) {
            return false;
        }

        return isOwnerOfMenuItem(inventory.getMenuItemId());
    }

    public boolean isOwnerOfMenuItem(Long menuItemId) {

        var menuItem = restaurantServiceClient
                .getMenuItemById(menuItemId);

        var restaurant = restaurantServiceClient
                .getRestaurantById(menuItem.getRestaurantId());

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(restaurant.getOwnerId());
    }
}