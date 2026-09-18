package com.foodflow.restaurantservice.Security;

import com.foodflow.restaurantservice.Entity.Restaurant;
import com.foodflow.restaurantservice.Entity.MenuItem;
import com.foodflow.restaurantservice.Repository.RestaurantRepo;
import com.foodflow.restaurantservice.Repository.MenuItemsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("restaurantAuthorization")
@RequiredArgsConstructor
public class RestaurantAuthorization {

    private final RestaurantRepo restaurantRepo;
    private final MenuItemsRepo menuItemsRepo;

    public boolean isOwner(Long restaurantId) {

        Restaurant restaurant =
                restaurantRepo.findById(restaurantId).orElse(null);

        if (restaurant == null) {
            return false;
        }

        Long currentUserId =
                SecurityUtils.getCurrentUserId();

        return currentUserId.equals(restaurant.getOwnerId());
    }

    public boolean ownsMenuItem(Long menuItemId) {

        MenuItem menuItem =
                menuItemsRepo.findById(menuItemId).orElse(null);

        if (menuItem == null) {
            return false;
        }

        Long currentUserId =
                SecurityUtils.getCurrentUserId();

        return currentUserId.equals(
                menuItem.getRestaurant().getOwnerId()
        );
    }
}