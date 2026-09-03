package com.example.search_service.repository;

import com.example.search_service.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<ProductDocument> searchProducts(
            String query,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean active,
            Pageable pageable) {

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        /*
         * Free-text search
         */
        if (query != null && !query.isBlank()) {

            mustQueries.add(
                    QueryBuilders.multiMatch()
                            .query(query)
                            .fields(
                                    "name^3",
                                    "sku^4",
                                    "category^2"
                            )
                            .fuzziness("AUTO")
                            .build()
                            ._toQuery()
            );

        } else {

            mustQueries.add(
                    QueryBuilders.matchAll()
                            .build()
                            ._toQuery()
            );
        }

        /*
         * Category filter
         */
        if (category != null && !category.isBlank()) {

            filterQueries.add(
                    QueryBuilders.term()
                            .field("category")
                            .value(category)
                            .build()
                            ._toQuery()
            );
        }

        /*
         * Minimum price
         */
        if (minPrice != null) {

            filterQueries.add(
                    QueryBuilders.range()
                            .field("price")
                            .gte(JsonData.of(minPrice.doubleValue()))
                            .build()
                            ._toQuery()
            );
        }

        /*
         * Maximum price
         */
        if (maxPrice != null) {

            filterQueries.add(
                    QueryBuilders.range()
                            .field("price")
                            .lte(JsonData.of(maxPrice.doubleValue()))
                            .build()
                            ._toQuery()
            );
        }

        /*
         * Active filter
         */
        if (active != null) {

            filterQueries.add(
                    QueryBuilders.term()
                            .field("active")
                            .value(active)
                            .build()
                            ._toQuery()
            );
        }

        /*
         * Build bool query
         */
        BoolQuery boolQuery = BoolQuery.of(builder -> builder
                .must(mustQueries)
                .filter(filterQueries)
        );

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(boolQuery._toQuery())
                .withPageable(pageable)
                .build();

        SearchHits<ProductDocument> searchHits =
                elasticsearchOperations.search(
                        searchQuery,
                        ProductDocument.class
                );

        List<ProductDocument> products = searchHits
                .getSearchHits()
                .stream()
                .map(hit -> hit.getContent())
                .toList();

        return new PageImpl<>(
                products,
                pageable,
                searchHits.getTotalHits()
        );
    }
}