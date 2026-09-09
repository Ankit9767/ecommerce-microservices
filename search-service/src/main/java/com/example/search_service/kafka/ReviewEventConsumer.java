package com.example.search_service.kafka;

import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.events.ReviewDeletedEvent;
import com.ecommerce.common.events.ReviewEvent;
import com.ecommerce.common.events.ReviewUpdatedEvent;
import com.ecommerce.common.exception.InvalidEventException;
import com.ecommerce.common.exception.MissingEventIdException;
import com.ecommerce.common.exception.MissingEventTypeException;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.search_service.service.ProductIndexingService;
import com.example.search_service.service.ReviewIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class ReviewEventConsumer {

    private final ProductIndexingService productIndexingService;

    private final ReviewIndexingService reviewIndexingService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.REVIEW_EVENTS,
            groupId = "search-group"
    )
    public void consume(ReviewEvent event) {

        validateEvent(event);

        log.info(
                "Received review event: " +
                        "eventId={}, eventType={}, reviewId={}, productId={}",
                event.getEventId(),
                event.getEventType(),
                event.getReviewId(),
                event.getProductId()
        );

        handleEvent(event);

        log.info(
                "Review event processed successfully: " +
                        "eventId={}, eventType={}, reviewId={}, productId={}",
                event.getEventId(),
                event.getEventType(),
                event.getReviewId(),
                event.getProductId()
        );
    }

    private void handleEvent(ReviewEvent event) {

        switch (event.getEventType()) {

            case REVIEW_CREATED -> {

                if (!(event instanceof ReviewCreatedEvent reviewEvent)) {
                    throw new InvalidEventException();
                }

                productIndexingService.createReviewRating(
                        reviewEvent.getProductId(),
                        reviewEvent.getReviewId(),
                        reviewEvent.getEventId().toString(),
                        reviewEvent.getRating()
                );

                reviewIndexingService.indexReview(reviewEvent);
            }

            case REVIEW_UPDATED -> {

                if (!(event instanceof ReviewUpdatedEvent reviewEvent)) {
                    throw new InvalidEventException();
                }

                productIndexingService.updateReviewRating(
                        reviewEvent.getProductId(),
                        reviewEvent.getReviewId(),
                        reviewEvent.getEventId().toString(),
                        reviewEvent.getOldRating(),
                        reviewEvent.getNewRating()
                );

                reviewIndexingService.updateReview(reviewEvent);
            }

            case REVIEW_DELETED -> {

                if (!(event instanceof ReviewDeletedEvent reviewEvent)) {
                    throw new InvalidEventException();
                }

                productIndexingService.deleteReviewRating(
                        reviewEvent.getProductId(),
                        reviewEvent.getReviewId(),
                        reviewEvent.getEventId().toString(),
                        reviewEvent.getRating()
                );

                reviewIndexingService.deleteReview(reviewEvent);
            }

            default -> log.warn(
                    "Ignoring unsupported review event type: " +
                            "eventId={}, eventType={}, reviewId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getReviewId()
            );
        }
    }

    private void validateEvent(ReviewEvent event) {

        if (event == null) {
            throw new InvalidEventException();
        }

        if (event.getEventId() == null) {
            throw new MissingEventIdException();
        }

        if (event.getEventType() == null) {
            throw new MissingEventTypeException();
        }

        if (event.getReviewId() == null) {
            throw new InvalidEventException();
        }

        if (event.getProductId() == null) {
            throw new InvalidEventException();
        }
    }

    @DltHandler
    public void handleDeadLetter(ReviewEvent event) {

        log.error(
                "Review event moved to DLT after retries exhausted: " +
                        "eventId={}, eventType={}, reviewId={}, productId={}, event={}",
                event != null ? event.getEventId() : null,
                event != null ? event.getEventType() : null,
                event != null ? event.getReviewId() : null,
                event != null ? event.getProductId() : null,
                event
        );
    }
}
