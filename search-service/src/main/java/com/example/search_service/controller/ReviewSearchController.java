package com.example.search_service.controller;

import com.example.search_service.document.ReviewSearchDocument;
import com.example.search_service.service.ReviewIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search/reviews")
@RequiredArgsConstructor
public class ReviewSearchController {

    private final ReviewIndexingService reviewSearchService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewSearchDocument>> getReviewsByProductId(
            @PathVariable Long productId, Pageable pageable) {

        return ResponseEntity.ok(
                reviewSearchService.getReviewsByProductId(
                        productId,
                        pageable
                )
        );
    }
}