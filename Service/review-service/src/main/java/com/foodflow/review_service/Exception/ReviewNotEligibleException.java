package com.foodflow.review_service.Exception;

public class ReviewNotEligibleException extends RuntimeException {
    public ReviewNotEligibleException(String message) {
        super(message);
    }
}
