package com.foodflow.review_service.Security;

import com.foodflow.review_service.Entity.Review;
import com.foodflow.review_service.Entity.ReviewEligibility;
import com.foodflow.review_service.Repository.ReviewEligibilityRepo;
import com.foodflow.review_service.Repository.ReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("reviewAuthorization")
@RequiredArgsConstructor
public class ReviewAuthorization {

    private final ReviewRepo reviewRepo;
    private final ReviewEligibilityRepo reviewEligibilityRepo;

    public boolean isOwner(Long reviewId) {

        Review review = reviewRepo.findById(reviewId)
                .orElse(null);

        if (review == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(review.getUserId());
    }

    public boolean isUser(Long userId) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        return currentUserId.equals(userId);
    }

    public boolean canCreateReview(Long orderId) {

        ReviewEligibility eligibility =
                reviewEligibilityRepo.findByOrderId(orderId)
                        .orElse(null);

        if (eligibility == null) {
            return false;
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        System.out.println("DEBUG orderId = " + orderId);
        System.out.println("DEBUG eligibilityUserId = " + eligibility.getUserId());
        System.out.println("DEBUG currentUserId = " + currentUserId);


        return currentUserId.equals(eligibility.getUserId());
    }
}