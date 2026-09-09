package com.example.reviews_service.service;

import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.events.ReviewDeletedEvent;
import com.ecommerce.common.events.ReviewUpdatedEvent;

public interface OutboxService {

    void saveReviewCreatedEvent(ReviewCreatedEvent event);

    void saveReviewUpdatedEvent(ReviewUpdatedEvent event);

    void saveReviewDeletedEvent(ReviewDeletedEvent event);
}