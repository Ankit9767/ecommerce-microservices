package com.example.search_service.repository;

import com.example.search_service.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, Long> {
}