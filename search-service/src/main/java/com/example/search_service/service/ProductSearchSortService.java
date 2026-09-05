package com.example.search_service.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProductSearchSortService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "price",
            "sku",
            "category",
            "active"
    );

    public Sort validateAndBuildSort(Sort sort) {

        if (sort == null || sort.isUnsorted()) {
            return Sort.by(
                    Sort.Order.asc("productId")
            );
        }

        return Sort.by(
                sort.stream()
                        .map(order -> {

                            String property = order.getProperty();

                            if (!ALLOWED_SORT_FIELDS.contains(property)) {
                                throw new IllegalArgumentException(
                                        "Unsupported sort field: " + property
                                );
                            }

                            if ("name".equals(property)) {
                                property = "name.keyword";
                            }

                            return new Sort.Order(
                                    order.getDirection(),
                                    property
                            );
                        })
                        .toList()
        );
    }
}