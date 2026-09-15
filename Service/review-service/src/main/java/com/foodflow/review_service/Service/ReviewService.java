package com.foodflow.review_service.Service;

import com.foodflow.common.Event.EventEnvelope;
import com.foodflow.common.Event.EventType;
import com.foodflow.common.Event.ReviewCreatedEvent;
import com.foodflow.review_service.Dto.CreateReviewRequest;
import com.foodflow.review_service.Dto.ReviewResponse;
import com.foodflow.review_service.Dto.UpdateReviewRequest;
import com.foodflow.review_service.Entity.Review;
import com.foodflow.review_service.Entity.ReviewEligibility;
import com.foodflow.review_service.Exception.DuplicateReviewException;
import com.foodflow.review_service.Exception.InvalidRatingException;
import com.foodflow.review_service.Exception.InvalidReviewException;
import com.foodflow.review_service.Exception.ReviewNotFoundException;
import com.foodflow.review_service.Kafka.ReviewKafkaProducer;
import com.foodflow.review_service.Repository.ReviewEligibilityRepo;
import com.foodflow.review_service.Repository.ReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepo reviewRepo;
    private final ReviewEligibilityRepo reviewEligibilityRepo;
    private final ReviewKafkaProducer reviewKafkaProducer;

    public ReviewResponse createReview(CreateReviewRequest request) {

        validateRating(request.getRating());

        if (reviewRepo.existsByOrderId(request.getOrderId())) {
            throw new DuplicateReviewException(
                    "Review already exists for order: " + request.getOrderId()
            );
        }

        ReviewEligibility eligibility =
                reviewEligibilityRepo
                        .findByOrderId(request.getOrderId())
                        .orElseThrow(() ->
                                new InvalidReviewException(
                                        "Order is not eligible for review: "
                                                + request.getOrderId()
                                )
                        );

        Review review = mapToEntity(request);

        review.setUserId(eligibility.getUserId());
        review.setRestaurantId(eligibility.getRestaurantId());

        Review savedReview = reviewRepo.save(review);

        ReviewCreatedEvent reviewCreatedEvent =
                new ReviewCreatedEvent(
                        savedReview.getId(),
                        savedReview.getOrderId(),
                        savedReview.getUserId(),
                        savedReview.getRestaurantId(),
                        savedReview.getRating()
                );

        EventEnvelope eventEnvelope =
                new EventEnvelope(
                        UUID.randomUUID().toString(),
                        EventType.REVIEW_CREATED.name(),
                        LocalDateTime.now(),
                        "review-service",
                        reviewCreatedEvent
                );

        reviewKafkaProducer.publishReviewCreated(eventEnvelope);

        return mapToResponse(savedReview);
    }

    public ReviewResponse getReviewById(Long reviewId) {

        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(
                                "Review not found with id: " + reviewId
                        )
                );

        return mapToResponse(review);
    }

    public List<ReviewResponse> getReviewsByRestaurant(Long restaurantId) {

        return reviewRepo.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ReviewResponse> getReviewsByUser(Long userId) {

        return reviewRepo.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReviewResponse updateReview(
            Long reviewId,
            UpdateReviewRequest request
    ) {

        validateRating(request.getRating());

        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(
                                "Review not found with id: " + reviewId
                        )
                );

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updatedReview = reviewRepo.save(review);

        return mapToResponse(updatedReview);
    }

    public void deleteReview(Long reviewId) {

        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(
                                "Review not found with id: " + reviewId
                        )
                );

        reviewRepo.delete(review);
    }

    //helper methods

    private void validateRating(Integer rating) {

        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidRatingException(
                    "Rating must be between 1 and 5"
            );
        }
    }

    private Review mapToEntity(CreateReviewRequest request) {

        return Review.builder()
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
    }

    private ReviewResponse mapToResponse(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .restaurantId(review.getRestaurantId())
                .orderId(review.getOrderId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

}
