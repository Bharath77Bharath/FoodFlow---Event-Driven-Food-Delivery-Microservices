package com.foodflow.restaurantservice.Service;

import com.foodflow.restaurantservice.Dto.MenuItemRequest;
import com.foodflow.restaurantservice.Dto.MenuItemResponse;
import com.foodflow.restaurantservice.Entity.MenuItem;
import com.foodflow.restaurantservice.Entity.Restaurant;
import com.foodflow.restaurantservice.Exception.MenuItemNotFoundException;
import com.foodflow.restaurantservice.Exception.RestaurantNotFoundException;
import com.foodflow.restaurantservice.Repository.MenuItemsRepo;
import com.foodflow.restaurantservice.Repository.RestaurantRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final RestaurantRepo restaurantRepo;
    private final MenuItemsRepo menuItemsRepo;

    private MenuItemResponse convertMenuItemResponse(MenuItem item) {

        MenuItemResponse response = new MenuItemResponse();

        response.setId(item.getId());
        response.setName(item.getName());
        response.setRestaurantId(item.getRestaurant().getId());
        response.setDescription(item.getDescription());
        response.setPrice(item.getPrice());
        response.setAvailable(item.getAvailable());

        return response;
    }

    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId).orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found!"));

        MenuItem item = new MenuItem();

        item.setRestaurant(restaurant);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setDescription(request.getDescription());
        if(request.getAvailable() == null) {
            item.setAvailable(true);
        } else {
            item.setAvailable(request.getAvailable());
        }

        MenuItem savedItem = menuItemsRepo.save(item);

        return convertMenuItemResponse(savedItem);
    }

    public MenuItemResponse getMenuItemById(Long menuItemId) {

        MenuItem response = menuItemsRepo.findById(menuItemId).orElseThrow(() -> new MenuItemNotFoundException("MenuItem not found!"));

        return convertMenuItemResponse(response);
    }

    public List<MenuItemResponse> getMenuItemByRestaurant(Long restaurantId) {
        if(!restaurantRepo.existsById(restaurantId)) {
            throw new RestaurantNotFoundException("Restaurant not found!");
        }

        List<MenuItem> menuItems = menuItemsRepo.findByRestaurantIdAndAvailableTrue(restaurantId);
        List<MenuItemResponse> menuItemResponses = new ArrayList<>();

        for(MenuItem menuItem : menuItems) {
            menuItemResponses.add(convertMenuItemResponse(menuItem));
        }

        return menuItemResponses;
    }

    public MenuItemResponse updateMenuItem(Long menuItemId,MenuItemRequest request) {
        MenuItem item = menuItemsRepo.findById(menuItemId).orElseThrow(() -> new MenuItemNotFoundException("MenuItem not found"));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        if(request.getAvailable() == null) item.setAvailable(true);
        else item.setAvailable(request.getAvailable());

        MenuItem updatedMenuItem = menuItemsRepo.save(item);

        return convertMenuItemResponse(updatedMenuItem);
    }

    public void deleteMenuItem(Long menuItemId) {
        MenuItem item = menuItemsRepo.findById(menuItemId).orElseThrow(() -> new MenuItemNotFoundException("MenuItem not found!"));

        item.setAvailable(false);

        menuItemsRepo.save(item);
    }
}
