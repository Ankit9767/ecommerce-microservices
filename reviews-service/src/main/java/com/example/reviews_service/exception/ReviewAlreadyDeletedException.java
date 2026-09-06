package com.example.reviews_service.exception;

public class ReviewAlreadyDeletedException extends RuntimeException {

    public ReviewAlreadyDeletedException(Long reviewId) {
        super("Review is already deleted with id: " + reviewId);
    }
}