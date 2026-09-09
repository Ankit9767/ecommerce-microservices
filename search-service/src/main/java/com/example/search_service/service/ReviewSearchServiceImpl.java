package com.example.search_service.service;

import com.example.search_service.document.ReviewSearchDocument;
import com.example.search_service.repository.ReviewSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewSearchServiceImpl implements ReviewSearchService {

    private final ReviewSearchRepository reviewSearchRepository;

    @Override
    public void createReview(Long reviewId, Long productId,
                             Long userId, Integer rating,
                             String title, String comment) {

        ReviewSearchDocument document =
                ReviewSearchDocument.builder()
                        .reviewId(reviewId)
                        .productId(productId)
                        .userId(userId)
                        .rating(rating)
                        .title(title)
                        .comment(comment)
                        .build();

        reviewSearchRepository.save(document);

        log.info(
                "Review indexed successfully: reviewId={}, productId={}",
                reviewId,
                productId
        );
    }

    @Override
    public void updateReview(Long reviewId, Long productId,
                             Long userId, Integer rating,
                             String title, String comment) {

        ReviewSearchDocument document =
                reviewSearchRepository.findById(reviewId)
                        .orElse(
                                ReviewSearchDocument.builder()
                                        .reviewId(reviewId)
                                        .build()
                        );

        document.setProductId(productId);
        document.setUserId(userId);
        document.setRating(rating);
        document.setTitle(title);
        document.setComment(comment);

        reviewSearchRepository.save(document);

        log.info(
                "Review search document updated: reviewId={}, productId={}",
                reviewId,
                productId
        );
    }

    @Override
    public void deleteReview(Long reviewId) {

        reviewSearchRepository.deleteById(reviewId);

        log.info(
                "Review search document deleted: reviewId={}",
                reviewId
        );
    }
}
