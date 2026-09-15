package com.foodflow.review_service.Repository;

import com.foodflow.review_service.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {

    boolean existsByOrderId(Long orderId);

    List<Review> findByRestaurantId(Long restaurantId);

    List<Review> findByUserId(Long userId);

}
