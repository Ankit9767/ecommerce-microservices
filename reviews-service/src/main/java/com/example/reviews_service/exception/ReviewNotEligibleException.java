package com.example.reviews_service.exception;

public class ReviewNotEligibleException extends RuntimeException {

    public ReviewNotEligibleException(String reason) {
        super(reason);
    }
}