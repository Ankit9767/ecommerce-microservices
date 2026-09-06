package com.example.reviews_service.controller;

import com.example.reviews_service.dto.CreateReviewRequest;
import com.example.reviews_service.dto.ReviewResponse;
import com.example.reviews_service.dto.UpdateReviewRequest;
import com.example.reviews_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request,
                                                       Authentication authentication) {

        ReviewResponse response =
                reviewService.createReview(
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long reviewId) {

        return ResponseEntity.ok(
                reviewService.getReview(reviewId)
        );
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(@PathVariable Long productId,
                                                                  @PageableDefault(size = 20, sort = "createdAt")
                                                                  Pageable pageable) {

        return ResponseEntity.ok(
                reviewService.getProductReviews(
                        productId,
                        pageable
                )
        );
    }

    @GetMapping("/me")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(Authentication authentication,
                                                             @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(
                reviewService.getMyReviews(
                        authentication,
                        pageable
                )
        );
    }

    @PatchMapping("/{reviewId}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long reviewId,
                                                       @Valid @RequestBody UpdateReviewRequest request,
                                                       Authentication authentication) {

        return ResponseEntity.ok(
                reviewService.updateReview(
                        reviewId,
                        request,
                        authentication
                )
        );
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             Authentication authentication) {

        reviewService.deleteReview(
                reviewId,
                authentication
        );

        return ResponseEntity.noContent().build();
    }
}