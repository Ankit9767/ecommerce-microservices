package com.example.reviews_service.exception;

public class ReviewEligibilityCheckException extends RuntimeException {

    public ReviewEligibilityCheckException() {
        super("Unable to verify review eligibility");
    }
}