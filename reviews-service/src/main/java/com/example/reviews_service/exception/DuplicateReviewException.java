package com.example.reviews_service.exception;

public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException(Long userId, Long productId, Long orderId) {
        super(
                "Review already exists for user " + userId
                        + ", product " + productId
                        + ", order " + orderId
        );
    }
}