package com.example.search_service.service;

import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.events.ReviewDeletedEvent;
import com.ecommerce.common.events.ReviewUpdatedEvent;
import com.example.search_service.document.ReviewSearchDocument;
import com.example.search_service.repository.ReviewSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewIndexingService {

    private final ReviewSearchRepository reviewSearchRepository;

    public Page<ReviewSearchDocument> getReviewsByProductId(Long productId,
                                                            Pageable pageable) {

        return reviewSearchRepository.findByProductId(
                productId,
                pageable
        );
    }

    public void indexReview(ReviewCreatedEvent event) {

        log.info(
                "Indexing review: reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );

        ReviewSearchDocument document =
                ReviewSearchDocument.builder()
                        .reviewId(event.getReviewId())
                        .productId(event.getProductId())
                        .userId(event.getUserId())
                        .rating(event.getRating())
                        .title(event.getTitle())
                        .comment(event.getComment())
                        .build();

        reviewSearchRepository.save(document);

        log.info(
                "Review indexed successfully: " +
                        "reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );
    }

    public void updateReview(ReviewUpdatedEvent event) {

        log.info(
                "Updating review in search index: " +
                        "reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );

        ReviewSearchDocument document =
                reviewSearchRepository.findById(event.getReviewId())
                        .orElse(
                                ReviewSearchDocument.builder()
                                        .reviewId(event.getReviewId())
                                        .build()
                        );

        document.setProductId(event.getProductId());
        document.setUserId(event.getUserId());
        document.setRating(event.getNewRating());
        document.setTitle(event.getTitle());
        document.setComment(event.getComment());

        reviewSearchRepository.save(document);

        log.info(
                "Review updated successfully in search index: " +
                        "reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );
    }

    public void deleteReview(ReviewDeletedEvent event) {

        log.info(
                "Deleting review from search index: " +
                        "reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );

        reviewSearchRepository.deleteById(event.getReviewId());

        log.info(
                "Review deleted successfully from search index: " +
                        "reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );
    }
}