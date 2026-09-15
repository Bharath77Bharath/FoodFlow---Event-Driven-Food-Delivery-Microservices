package com.foodflow.review_service.Repository;

import com.foodflow.review_service.Entity.ReviewEligibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewEligibilityRepo
        extends JpaRepository<ReviewEligibility, Long> {

    Optional<ReviewEligibility> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}