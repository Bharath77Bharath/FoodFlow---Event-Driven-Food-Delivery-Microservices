package com.foodflow.restaurantservice.Repository;

import com.foodflow.restaurantservice.Entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByActiveTrue();

    List<Restaurant> findByCityIgnoreCaseAndActiveTrue(String city);

    List<Restaurant> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
