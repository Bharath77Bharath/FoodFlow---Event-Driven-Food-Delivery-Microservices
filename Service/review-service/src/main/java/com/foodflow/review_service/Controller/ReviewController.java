package com.foodflow.review_service.Controller;

import com.foodflow.review_service.Dto.CreateReviewRequest;
import com.foodflow.review_service.Dto.ReviewResponse;
import com.foodflow.review_service.Dto.UpdateReviewRequest;
import com.foodflow.review_service.Service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.CacheResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("""
        hasRole('CUSTOMER') and
        @reviewAuthorization.canCreateReview(#request.orderId)
        """)
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("""
        hasAnyRole(
            'CUSTOMER',
            'RESTAURANT_OWNER',
            'DELIVERY_PARTNER',
            'ADMIN'
        )
        """)
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReviewById(reviewId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/restaurants/{restaurantId}")
    @PreAuthorize("""
        hasAnyRole(
            'CUSTOMER',
            'RESTAURANT_OWNER',
            'DELIVERY_PARTNER',
            'ADMIN'
        )
        """)
    public ResponseEntity<List<ReviewResponse>> getReviewByRestaurantId(@PathVariable Long restaurantId) {
        List<ReviewResponse> reviewResponses = reviewService.getReviewsByRestaurant(restaurantId);

        return new ResponseEntity<>(reviewResponses, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("""
        hasRole('ADMIN') or
        @reviewAuthorization.isUser(#userId)
        """)
    public ResponseEntity<List<ReviewResponse>> getReviewByUserId(@PathVariable Long userId) {
        List<ReviewResponse> reviewResponses = reviewService.getReviewsByUser(userId);

        return new ResponseEntity<>(reviewResponses, HttpStatus.OK);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("""
        hasRole('ADMIN') or
        @reviewAuthorization.isOwner(#reviewId)
        """)
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long reviewId, @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(reviewId, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("""
        hasRole('ADMIN') or
        @reviewAuthorization.isOwner(#reviewId)
        """)
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);

        return ResponseEntity.noContent().build();
    }
}
