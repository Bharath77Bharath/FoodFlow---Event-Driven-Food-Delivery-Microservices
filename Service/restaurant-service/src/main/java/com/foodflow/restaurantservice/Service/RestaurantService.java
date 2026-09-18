package com.foodflow.restaurantservice.Service;

import com.foodflow.restaurantservice.Client.UserServiceClient;
import com.foodflow.restaurantservice.Dto.RestaurantRequest;
import com.foodflow.restaurantservice.Dto.RestaurantResponse;
import com.foodflow.restaurantservice.Entity.Restaurant;
import com.foodflow.restaurantservice.Exception.RestaurantNotFoundException;
import com.foodflow.restaurantservice.Repository.RestaurantRepo;
import com.foodflow.restaurantservice.Security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepo restaurantRepo;
    private final UserServiceClient userServiceClient;

    public RestaurantResponse convertRestaurantResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();

        response.setId(restaurant.getId());
        response.setOwnerId(restaurant.getOwnerId());
        response.setName(restaurant.getName());
        response.setDescription(restaurant.getDescription());
        response.setAddress(restaurant.getAddress());
        response.setCity(restaurant.getCity());
        response.setPhone(restaurant.getPhone());
        response.setEmail(restaurant.getEmail());
        response.setActive(restaurant.getActive());
        response.setCreatedAt(restaurant.getCreatedAt());
        response.setUpdatedAt(restaurant.getUpdatedAt());

        return response;
    }

    public RestaurantResponse createRestaurant(RestaurantRequest request) {

        Long currentUser = SecurityUtils.getCurrentUserId();

        userServiceClient.getUserById(currentUser);

        Restaurant restaurant = new Restaurant();

        restaurant.setOwnerId(currentUser);
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setEmail(request.getEmail());
        restaurant.setPhone(request.getPhone());

        Restaurant savedRestaurant = restaurantRepo.save(restaurant);

        return convertRestaurantResponse(savedRestaurant);
    }

    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepo.findById(id).orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found!"));

        return convertRestaurantResponse(restaurant);
    }

    public List<RestaurantResponse> getRestaurants() {
        List<Restaurant> restaurantList = restaurantRepo.findByActiveTrue();
        List<RestaurantResponse> responseList = new ArrayList<>();

        for(Restaurant restaurant : restaurantList) {
            RestaurantResponse response = convertRestaurantResponse(restaurant);

            responseList.add(response);
        }

        return responseList;
    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepo.findById(id).orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found!"));

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setEmail(request.getEmail());
        restaurant.setPhone(request.getPhone());

        Restaurant savedRestaurant = restaurantRepo.save(restaurant);

        return convertRestaurantResponse(savedRestaurant);
    }

    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepo.findById(id).orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found!"));

        restaurant.setActive(false);

        restaurantRepo.save(restaurant);
    }

    public boolean isRestaurantActive(Long id) {
        Restaurant restaurant = restaurantRepo.findById(id).orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found!"));

        return Boolean.TRUE.equals(restaurant.getActive());
    }

    public List<RestaurantResponse> searchByCity(String city) {
        List<Restaurant> restaurantList = restaurantRepo.findByCityIgnoreCaseAndActiveTrue(city);
        List<RestaurantResponse> responses = new ArrayList<>();
        for(Restaurant restaurant : restaurantList) {
            responses.add(convertRestaurantResponse(restaurant));
        }

        return responses;
    }

    public List<RestaurantResponse> searchByName(String name) {
        List<Restaurant> restaurantList = restaurantRepo.findByNameContainingIgnoreCaseAndActiveTrue(name);
        List<RestaurantResponse> responses = new ArrayList<>();
        for(Restaurant restaurant : restaurantList) {
            responses.add(convertRestaurantResponse(restaurant));
        }

        return responses;
    }
}
