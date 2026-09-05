package com.example.search_service.service;

import com.example.search_service.document.ProductDocument;
import com.example.search_service.exception.InvalidSearchParameterException;
import com.example.search_service.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

    private final ProductSearchSortService productSearchSortService;

    public Page<ProductDocument> searchProducts(
            String query,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Pageable pageable) {

        validatePriceRange(minPrice, maxPrice);

        Sort validatedSort =
                productSearchSortService.validateAndBuildSort(
                        pageable.getSort()
                );

        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                validatedSort
        );

        return productSearchRepository.searchProducts(
                query,
                category,
                minPrice,
                maxPrice,
                active,
                sortedPageable
        );
    }

    private void validatePriceRange(BigDecimal minPrice,
                                    BigDecimal maxPrice) {

        if (minPrice != null && minPrice.signum() < 0) {
            throw new InvalidSearchParameterException(
                    "minPrice cannot be negative"
            );
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new InvalidSearchParameterException(
                    "maxPrice cannot be negative"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new InvalidSearchParameterException(
                    "minPrice cannot be greater than maxPrice"
            );
        }
    }
}