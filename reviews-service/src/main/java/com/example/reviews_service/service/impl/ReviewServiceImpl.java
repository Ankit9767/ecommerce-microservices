package com.example.reviews_service.service.impl;

import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.events.ReviewDeletedEvent;
import com.ecommerce.common.events.ReviewUpdatedEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.security.CurrentUser;
import com.example.reviews_service.client.OrderClient;
import com.example.reviews_service.dto.CreateReviewRequest;
import com.ecommerce.common.dto.ReviewEligibilityResponse;
import com.example.reviews_service.dto.ReviewResponse;
import com.example.reviews_service.dto.UpdateReviewRequest;
import com.example.reviews_service.entity.Review;
import com.example.reviews_service.entity.ReviewStatus;
import com.example.reviews_service.exception.*;
import com.example.reviews_service.repository.ReviewRepository;
import com.example.reviews_service.service.OutboxService;
import com.example.reviews_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final CurrentUser currentUser;

    private final OrderClient orderClient;

    private final OutboxService outboxService;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request,
                                       Authentication authentication) {

        Long userId = currentUser.getUserId(authentication);

        ReviewEligibilityResponse eligibility;

        try {

            eligibility = orderClient.checkReviewEligibility(
                    request.getOrderId(),
                    request.getProductId(),
                    userId
            );

        } catch (RuntimeException ex) {

            throw new ReviewEligibilityCheckException();

        }

        if (eligibility == null || !eligibility.eligible()) {

            String reason =
                    eligibility != null && eligibility.reason() != null
                            ? eligibility.reason()
                            : "User is not eligible to review this product";

            throw new ReviewNotEligibleException(reason);
        }

        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(
                userId,
                request.getProductId(),
                request.getOrderId()
        )) {
            throw new DuplicateReviewException(
                    userId,
                    request.getProductId(),
                    request.getOrderId()
            );
        }

        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(userId)
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .status(ReviewStatus.ACTIVE)
                .build();

        Review savedReview = reviewRepository.save(review);

        writeReviewCreatedToOutbox(savedReview);

        return ReviewResponse.from(savedReview);
    }

    private void writeReviewCreatedToOutbox(Review review) {

        ReviewCreatedEvent event =
                ReviewCreatedEvent.builder()
                        .eventType(EventType.REVIEW_CREATED)
                        .reviewId(review.getId())
                        .productId(review.getProductId())
                        .userId(review.getUserId())
                        .orderId(review.getOrderId())
                        .rating(review.getRating())
                        .title(review.getTitle())
                        .comment(review.getComment())
                        .build();

        outboxService.saveReviewCreatedEvent(event);
    }


    @Override
    public ReviewResponse getReview(Long reviewId) {

        Review review = findActiveReview(reviewId);

        return ReviewResponse.from(review);
    }

    @Override
    public Page<ReviewResponse> getProductReviews(Long productId,
                                                  Pageable pageable) {

        return reviewRepository.findByProductIdAndStatus(
                        productId,
                        ReviewStatus.ACTIVE,
                        pageable
                )
                .map(ReviewResponse::from);
    }

    @Override
    public Page<ReviewResponse> getMyReviews(Authentication authentication,
                                             Pageable pageable) {

        Long userId = currentUser.getUserId(authentication);

        return reviewRepository.findByUserIdAndStatus(
                        userId,
                        ReviewStatus.ACTIVE,
                        pageable
                )
                .map(ReviewResponse::from);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId,
                                       UpdateReviewRequest request,
                                       Authentication authentication) {

        Long currentUserId = currentUser.getUserId(authentication);

        Review review = findReview(reviewId);

        validateOwnership(review, currentUserId);

        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ReviewAlreadyDeletedException(reviewId);
        }

        Integer oldRating = review.getRating();

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);

        writeReviewUpdatedToOutbox(updatedReview, oldRating);

        return ReviewResponse.from(updatedReview);

    }

    private void writeReviewUpdatedToOutbox(Review review,
                                            Integer oldRating) {

        ReviewUpdatedEvent event =
                ReviewUpdatedEvent.builder()
                        .eventType(EventType.REVIEW_UPDATED)
                        .reviewId(review.getId())
                        .productId(review.getProductId())
                        .userId(review.getUserId())
                        .orderId(review.getOrderId())
                        .oldRating(oldRating)
                        .newRating(review.getRating())
                        .title(review.getTitle())
                        .comment(review.getComment())
                        .build();

        outboxService.saveReviewUpdatedEvent(event);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Authentication authentication) {

        Long currentUserId = currentUser.getUserId(authentication);

        Review review = findReview(reviewId);

        validateOwnership(review, currentUserId);

        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ReviewAlreadyDeletedException(reviewId);
        }

        Integer rating = review.getRating();

        review.setStatus(ReviewStatus.DELETED);

        reviewRepository.save(review);

        writeReviewDeletedToOutbox(review, rating);

    }

    private void writeReviewDeletedToOutbox(Review review, Integer rating) {

        ReviewDeletedEvent event =
                ReviewDeletedEvent.builder()
                        .eventType(EventType.REVIEW_DELETED)
                        .reviewId(review.getId())
                        .productId(review.getProductId())
                        .userId(review.getUserId())
                        .orderId(review.getOrderId())
                        .rating(rating)
                        .build();

        outboxService.saveReviewDeletedEvent(event);
    }

    /**
     * Finds a review regardless of its status.
     *
     * Used by update/delete operations because those operations need
     * to distinguish a missing review from an already deleted review.
     */
    private Review findReview(Long reviewId) {

        return reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(reviewId)
                );
    }

    /**
     * Finds only an active review.
     *
     * Used by public read operations so soft-deleted reviews are not
     * exposed through GET /reviews/{id}.
     */
    private Review findActiveReview(Long reviewId) {

        Review review = findReview(reviewId);

        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ReviewNotFoundException(reviewId);
        }

        return review;
    }

    private void validateOwnership(Review review,
                                   Long currentUserId) {

        if (!review.getUserId().equals(currentUserId)) {
            throw new ReviewAccessDeniedException();
        }
    }
}