package com.example.search_service.repository;

import com.example.search_service.document.ReviewSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReviewSearchRepository
        extends ElasticsearchRepository<ReviewSearchDocument, Long> {
}