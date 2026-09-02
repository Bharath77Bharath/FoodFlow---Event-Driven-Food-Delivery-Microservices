package com.foodflow.restaurantservice.Repository;

import com.foodflow.restaurantservice.Entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemsRepo extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);
}
