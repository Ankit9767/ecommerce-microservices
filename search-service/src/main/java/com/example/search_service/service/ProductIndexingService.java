package com.example.search_service.service;

import com.ecommerce.common.events.*;
import com.example.search_service.document.ProductDocument;
import com.example.search_service.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexingService {

    private final ProductSearchRepository productSearchRepository;

    private final ReviewSearchService reviewSearchService;

    public void indexProduct(ProductCreatedEvent event) {

        log.info(
                "Indexing product: productId={}, sku={}, name={}",
                event.getProductId(),
                event.getSku(),
                event.getName()
        );

        ProductDocument document =
                ProductDocument.builder()
                        .productId(event.getProductId())
                        .name(event.getName())
                        .sku(event.getSku())
                        .category(event.getCategory())
                        .price(event.getPrice())
                        .active(event.getActive())
                        .ratingSum(0.0)
                        .reviewCount(0L)
                        .averageRating(0.0)
                        .build();

        productSearchRepository.save(document);

        log.info(
                "Product indexed successfully: productId={}",
                event.getProductId()
        );
    }

    public void updateProduct(ProductUpdatedEvent event) {

        log.info(
                "Updating product in search index: " +
                        "productId={}, sku={}, name={}",
                event.getProductId(),
                event.getSku(),
                event.getName()
        );

        ProductDocument document =
                productSearchRepository.findById(event.getProductId())
                        .orElseGet(() ->
                                ProductDocument.builder()
                                        .productId(event.getProductId())
                                        .ratingSum(0.0)
                                        .reviewCount(0L)
                                        .averageRating(0.0)
                                        .build()
                        );

        document.setName(event.getName());
        document.setSku(event.getSku());
        document.setCategory(event.getCategory());
        document.setPrice(event.getPrice());
        document.setActive(event.getActive());

        productSearchRepository.save(document);

        log.info(
                "Product updated successfully in search index: " +
                        "productId={}",
                event.getProductId()
        );
    }

    public void deleteProduct(ProductDeletedEvent event) {

        log.info(
                "Deleting product from search index: productId={}",
                event.getProductId()
        );

        productSearchRepository.deleteById(event.getProductId());

        log.info(
                "Product deleted from search index: productId={}",
                event.getProductId()
        );
    }

    public void indexReview(ReviewCreatedEvent event) {

        productSearchRepository.createReviewRating(
                event.getProductId(),
                event.getReviewId(),
                event.getEventId().toString(),
                event.getRating()
        );

        reviewSearchService.createReview(
                event.getReviewId(),
                event.getProductId(),
                event.getUserId(),
                event.getRating(),
                event.getTitle(),
                event.getComment()
        );

        log.info(
                "Review indexed successfully: " +
                        "reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );
    }

    public void updateReview(ReviewUpdatedEvent event) {

        productSearchRepository.updateReviewRating(
                event.getProductId(),
                event.getReviewId(),
                event.getEventId().toString(),
                event.getOldRating(),
                event.getNewRating()
        );

        reviewSearchService.updateReview(
                event.getReviewId(),
                event.getProductId(),
                event.getUserId(),
                event.getNewRating(),
                event.getTitle(),
                event.getComment()
        );

        log.info(
                "Review search document updated: " +
                        "reviewId={}, productId={}, oldRating={}, newRating={}",
                event.getReviewId(),
                event.getProductId(),
                event.getOldRating(),
                event.getNewRating()
        );
    }

    public void deleteReview(ReviewDeletedEvent event) {

        productSearchRepository.deleteReviewRating(
                event.getProductId(),
                event.getReviewId(),
                event.getEventId().toString(),
                event.getRating()
        );

        reviewSearchService.deleteReview(
                event.getReviewId()
        );

        log.info(
                "Review search document deleted: " +
                        "reviewId={}, productId={}, rating={}",
                event.getReviewId(),
                event.getProductId(),
                event.getRating()
        );
    }
}