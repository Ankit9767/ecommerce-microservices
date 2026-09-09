package com.example.search_service.service;

public interface ReviewSearchService {

    void createReview(Long reviewId, Long productId,
                      Long userId, Integer rating,
                      String title, String comment);

    void updateReview(
            Long reviewId, Long productId,
            Long userId, Integer rating,
            String title, String comment);

    void deleteReview(Long reviewId);
}