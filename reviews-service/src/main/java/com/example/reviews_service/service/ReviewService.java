package com.example.reviews_service.service;

import com.example.reviews_service.dto.CreateReviewRequest;
import com.example.reviews_service.dto.ReviewResponse;
import com.example.reviews_service.dto.UpdateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface ReviewService {

    ReviewResponse createReview(CreateReviewRequest request,
                                Authentication authentication);

    ReviewResponse getReview(Long reviewId);

    Page<ReviewResponse> getProductReviews(Long productId,
                                           Pageable pageable);

    Page<ReviewResponse> getMyReviews(Authentication authentication,
                                      Pageable pageable);

    ReviewResponse updateReview(Long reviewId,
                                UpdateReviewRequest request,
                                Authentication authentication);

    void deleteReview(Long reviewId,
                      Authentication authentication);
}