package com.example.reviews_service.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long reviewId) {
        super("Review not found with id: " + reviewId);
    }
}