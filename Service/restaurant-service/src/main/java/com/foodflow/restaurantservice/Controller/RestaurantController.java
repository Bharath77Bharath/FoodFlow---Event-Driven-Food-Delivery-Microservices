package com.foodflow.restaurantservice.Controller;

import com.foodflow.restaurantservice.Dto.RestaurantRequest;
import com.foodflow.restaurantservice.Dto.RestaurantResponse;
import com.foodflow.restaurantservice.Service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping()
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.createRestaurant(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.getRestaurantById(id);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<List<RestaurantResponse>> getRestaurants() {
        List<RestaurantResponse> responseList = restaurantService.getRestaurants();

        return new ResponseEntity<>(responseList,HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @restaurantAuthorization.isOwner(#id)")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable Long id,@RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.updateRestaurant(id,request);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @restaurantAuthorization.isOwner(#id)")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<Boolean> isRestaurantActive(@PathVariable Long id) {
        boolean active = restaurantService.isRestaurantActive(id);

        return new ResponseEntity<>(active,HttpStatus.OK);
    }

    @GetMapping("/search/city")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<List<RestaurantResponse>> searchByCity(@RequestParam String city) {
        List<RestaurantResponse> responses = restaurantService.searchByCity(city);

        return new ResponseEntity<>(responses,HttpStatus.OK);
    }

    @GetMapping("/search/name")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ResponseEntity<List<RestaurantResponse>> searchByName(@RequestParam String name) {
        List<RestaurantResponse> responses = restaurantService.searchByName(name);

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
