package com.example.reviews_service.kafka;

import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ReviewEventProducer {

    private final KafkaTemplate<String, ReviewCreatedEvent> kafkaTemplate;

    private final Counter publishedCounter;

    public ReviewEventProducer(KafkaTemplate<String, ReviewCreatedEvent> kafkaTemplate,
                               MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter = Counter.builder(
                        "kafka.review.events.published"
                )
                .description(
                        "Number of review events successfully published to Kafka"
                )
                .register(meterRegistry);
    }

    public CompletableFuture<?> publish(ReviewCreatedEvent event) {

        String reviewId = event.getReviewId().toString();

        String topic = resolveTopic(event.getEventType());

        log.debug(
                "Publishing review event: eventType={}, reviewId={}, topic={}",
                event.getEventType(),
                reviewId,
                topic
        );

        return kafkaTemplate
                .send(
                        topic,
                        reviewId,
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {

                        log.error(
                                "Failed to publish review event: " +
                                        "eventType={}, reviewId={}, topic={}",
                                event.getEventType(),
                                reviewId,
                                topic,
                                throwable
                        );

                        return;
                    }

                    publishedCounter.increment();

                    log.debug(
                            "Successfully published review event: " +
                                    "eventType={}, reviewId={}, topic={}, " +
                                    "partition={}, offset={}",
                            event.getEventType(),
                            reviewId,
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }

    private String resolveTopic(EventType eventType) {

        return switch (eventType) {

            case REVIEW_CREATED ->
                    KafkaTopics.REVIEW_CREATED;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported review event type: " + eventType
                    );
        };
    }
}