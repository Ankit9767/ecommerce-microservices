package com.ecommerce.common.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.exception.EventIdempotencyException;
import com.ecommerce.common.exception.InvalidEventIdException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventIdempotencyService {

    private final ProcessedEventRepository repository;

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(DomainEvent event) {

        UUID eventId = validateAndGetEventId(event);

        try {

            return repository.existsByEventId(
                    eventId.toString()
            );

        } catch (DataAccessException ex) {

            log.error(
                    "Failed to check event idempotency: eventId={}, eventType={}",
                    eventId,
                    getEventType(event),
                    ex
            );

            throw new EventIdempotencyException(
                    "Unable to check whether event has already been processed: "
                            + eventId,
                    ex
            );
        }
    }

    @Transactional
    public boolean markProcessed(DomainEvent event) {

        UUID eventId = validateAndGetEventId(event);

        String eventType = getEventType(event);

        try {

            ProcessedEvent processedEvent =
                    ProcessedEvent.builder()
                            .eventId(eventId.toString())
                            .eventType(eventType)
                            .processedAt(Instant.now())
                            .build();

            repository.saveAndFlush(processedEvent);

            log.debug(
                    "Event marked as processed: eventId={}, eventType={}",
                    eventId,
                    eventType
            );

            return true;

        } catch (DataIntegrityViolationException ex) {

            /*
             * The expected duplicate case:
             *
             * Transaction A -> inserts eventId
             * Transaction B -> tries same eventId
             * Transaction B -> UNIQUE constraint violation
             *
             * Therefore the event has already been recorded.
             */
            if (existsSafely(eventId)) {

                log.info(
                        "Duplicate event detected while marking processed: " +
                                "eventId={}, eventType={}",
                        eventId,
                        eventType
                );

                return false;
            }

            log.error(
                    "Data integrity violation while marking event as processed: " +
                            "eventId={}, eventType={}",
                    eventId,
                    eventType,
                    ex
            );

            throw new EventIdempotencyException(
                    "Failed to mark event as processed: " + eventId,
                    ex
            );

        } catch (DataAccessException ex) {

            log.error(
                    "Database error while marking event as processed: " +
                            "eventId={}, eventType={}",
                    eventId,
                    eventType,
                    ex
            );

            throw new EventIdempotencyException(
                    "Unable to mark event as processed: " + eventId,
                    ex
            );
        }
    }

    @Transactional
    public boolean shouldProcess(DomainEvent event) {

        UUID eventId = validateAndGetEventId(event);

        if (alreadyProcessed(event)) {

            log.info(
                    "Ignoring already processed event: eventId={}, eventType={}",
                    eventId,
                    getEventType(event)
            );

            return false;
        }

        return markProcessed(event);
    }

    private UUID validateAndGetEventId(DomainEvent event) {

        if (event == null) {

            throw new InvalidEventIdException(
                    "Domain event must not be null"
            );
        }

        if (event.getEventId() == null) {

            throw new InvalidEventIdException(
                    "Domain event must contain an eventId"
            );
        }

        return event.getEventId();
    }

    private boolean existsSafely(UUID eventId) {

        try {

            return repository.existsByEventId(
                    eventId.toString()
            );

        } catch (DataAccessException ex) {

            log.error(
                    "Unable to verify duplicate event after database constraint " +
                            "violation: eventId={}",
                    eventId,
                    ex
            );

            throw new EventIdempotencyException(
                    "Unable to verify event idempotency: " + eventId,
                    ex
            );
        }
    }

    private String getEventType(DomainEvent event) {

        if (event == null || event.getEventType() == null) {
            return null;
        }

        return event.getEventType().getValue();
    }
}