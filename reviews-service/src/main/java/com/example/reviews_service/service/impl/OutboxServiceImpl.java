package com.example.reviews_service.service.impl;

import com.ecommerce.common.events.ReviewCreatedEvent;
import com.ecommerce.common.exception.OutboxEventCreationException;
import com.ecommerce.common.kafka.EventType;
import com.ecommerce.common.kafka.OutboxEvent;
import com.ecommerce.common.kafka.OutboxRepository;
import com.example.reviews_service.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxRepository repository;

    private final ObjectMapper objectMapper;

    @Override
    public void saveReviewCreatedEvent(ReviewCreatedEvent event) {

        save(EventType.REVIEW_CREATED, event, event.getReviewId());
    }

    private void save(EventType eventType, Object event, Long aggregateId) {

        try {

            OutboxEvent outbox = OutboxEvent.builder()
                    .eventType(eventType.getValue())
                    .aggregateId(aggregateId)
                    .payload(objectMapper.writeValueAsString(event))
                    .published(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            repository.save(outbox);

        } catch (JsonProcessingException ex) {

            throw new OutboxEventCreationException(
                    "Failed to serialize outbox event: "
                            + eventType
                            + " for aggregate "
                            + aggregateId,
                    ex
            );
        }
    }
}