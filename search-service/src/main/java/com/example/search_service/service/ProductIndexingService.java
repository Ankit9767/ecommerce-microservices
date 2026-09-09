package com.example.search_service.service;

import com.ecommerce.common.events.ProductCreatedEvent;
import com.ecommerce.common.events.ProductDeletedEvent;
import com.ecommerce.common.events.ProductUpdatedEvent;
import com.ecommerce.common.events.ReviewCreatedEvent;
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

        productSearchRepository.updateReviewRating(
                event.getProductId(),
                event.getReviewId(),
                event.getRating()
        );

        log.info(
                "Product rating updated successfully: " +
                        "reviewId={}, productId={}",
                event.getReviewId(),
                event.getProductId()
        );
    }
}