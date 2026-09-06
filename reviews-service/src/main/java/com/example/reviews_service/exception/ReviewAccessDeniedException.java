package com.example.reviews_service.exception;

public class ReviewAccessDeniedException extends RuntimeException {

    public ReviewAccessDeniedException() {
        super("You are not allowed to modify this review");
    }
}