package com.example.reviews_service.service;

import com.ecommerce.common.events.ReviewCreatedEvent;

public interface OutboxService {

    void saveReviewCreatedEvent(ReviewCreatedEvent event);
}