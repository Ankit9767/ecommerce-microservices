package com.example.search_service.kafka;

import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.exception.InvalidEventException;
import com.ecommerce.common.exception.MissingEventIdException;
import com.ecommerce.common.exception.MissingEventTypeException;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import com.example.search_service.service.ProductIndexingService;
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
public class ReviewCreatedConsumer {

    private final ProductIndexingService productIndexingService;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 3000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = KafkaTopics.REVIEW_CREATED,
            groupId = "search-group"
    )
    public void consume(ReviewCreatedEvent event) {

        validateEvent(event);

        log.info(
                "Received ReviewCreatedEvent: " +
                        "eventId={}, eventType={}, reviewId={}, productId={}",
                event.getEventId(),
                event.getEventType(),
                event.getReviewId(),
                event.getProductId()
        );

        if (event.getEventType() != EventType.REVIEW_CREATED) {

            log.warn(
                    "Ignoring unexpected review event type: " +
                            "eventId={}, eventType={}, reviewId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getReviewId()
            );

            return;
        }

        productIndexingService.indexReview(event);

        log.info(
                "ReviewCreatedEvent processed successfully: " +
                        "eventId={}, reviewId={}, productId={}",
                event.getEventId(),
                event.getReviewId(),
                event.getProductId()
        );
    }

    private void validateEvent(ReviewCreatedEvent event) {

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

        if (event.getRating() == null) {
            throw new InvalidEventException();
        }
    }

    @DltHandler
    public void handleDeadLetter(ReviewCreatedEvent event) {

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