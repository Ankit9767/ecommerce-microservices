package com.example.search_service.repository;

import com.example.search_service.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, Long>, ProductSearchRepositoryCustom {

    Page<ProductDocument> findByNameContainingIgnoreCase(String name,
                                                         Pageable pageable);

    Page<ProductDocument> findByCategoryIgnoreCase(String category,
                                                   Pageable pageable);

    Page<ProductDocument> findByActive(Boolean active,
                                       Pageable pageable);
}