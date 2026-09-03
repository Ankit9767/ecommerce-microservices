package com.example.search_service.service;

import com.example.search_service.document.ProductDocument;
import com.example.search_service.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

    public Page<ProductDocument> searchProducts(
            String query,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Pageable pageable) {

        return productSearchRepository.searchProducts(
                query,
                category,
                minPrice,
                maxPrice,
                active,
                pageable
        );
    }
}