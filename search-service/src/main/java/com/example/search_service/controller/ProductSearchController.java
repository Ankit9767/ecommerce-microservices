package com.example.search_service.controller;

import com.example.search_service.dto.ProductSearchResponse;
import com.example.search_service.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/search/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping
    public Page<ProductSearchResponse> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {

        return productSearchService
                .searchProducts(
                        q,
                        category,
                        minPrice,
                        maxPrice,
                        active,
                        pageable
                )
                .map(ProductSearchResponse::from);
    }
}