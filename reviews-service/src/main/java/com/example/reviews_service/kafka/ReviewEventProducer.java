package com.example.reviews_service.kafka;

import com.ecommerce.common.events.ReviewEvent;
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

    private static final String REVIEW_EVENTS_TOPIC =
            KafkaTopics.REVIEW_EVENTS;

    private final KafkaTemplate<String, ReviewEvent> kafkaTemplate;

    private final Counter publishedCounter;

    private final Counter failedCounter;

    public ReviewEventProducer(
            KafkaTemplate<String, ReviewEvent> kafkaTemplate,
            MeterRegistry meterRegistry) {

        this.kafkaTemplate = kafkaTemplate;

        this.publishedCounter = Counter.builder(
                        "kafka.review.events.published"
                )
                .description(
                        "Number of review events successfully published to Kafka"
                )
                .tag("topic", REVIEW_EVENTS_TOPIC)
                .register(meterRegistry);

        this.failedCounter = Counter.builder(
                        "kafka.review.events.failed"
                )
                .description(
                        "Number of review events that failed to publish to Kafka"
                )
                .tag("topic", REVIEW_EVENTS_TOPIC)
                .register(meterRegistry);
    }

    public CompletableFuture<?> publish(ReviewEvent event) {

        String reviewId = event.getReviewId().toString();

        log.debug(
                "Publishing review event: " +
                        "reviewId={}, eventType={}, topic={}",
                reviewId,
                event.getEventType(),
                REVIEW_EVENTS_TOPIC
        );

        return kafkaTemplate
                .send(
                        REVIEW_EVENTS_TOPIC,
                        event.getProductId().toString(),
                        event
                )
                .whenComplete((result, throwable) -> {

                    if (throwable != null) {

                        failedCounter.increment();

                        log.error(
                                "Failed to publish review event: " +
                                        "reviewId={}, eventType={}, topic={}",
                                reviewId,
                                event.getEventType(),
                                REVIEW_EVENTS_TOPIC,
                                throwable
                        );

                        return;
                    }

                    publishedCounter.increment();

                    log.debug(
                            "Successfully published review event: " +
                                    "reviewId={}, eventType={}, topic={}, " +
                                    "partition={}, offset={}",
                            reviewId,
                            event.getEventType(),
                            REVIEW_EVENTS_TOPIC,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}