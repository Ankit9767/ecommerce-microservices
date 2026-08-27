package com.ecommerce.common.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.ecommerce.common.exception.InvalidEventIdException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        UUID eventId = getEventId(event);

        return repository.existsByEventId(
                eventId.toString()
        );
    }

    @Transactional
    public boolean markProcessed(DomainEvent event) {

        UUID eventId = getEventId(event);

        try {

            ProcessedEvent processedEvent =
                    ProcessedEvent.builder()
                            .eventId(eventId.toString())
                            .eventType(
                                    event.getEventType() != null
                                            ? event.getEventType().getValue()
                                            : null
                            )
                            .processedAt(Instant.now())
                            .build();

            repository.saveAndFlush(processedEvent);

            log.debug(
                    "Marked event as processed: eventId={}, eventType={}",
                    eventId,
                    event.getEventType()
            );

            return true;

        } catch (DataIntegrityViolationException ex) {

            /*
             * Most likely another consumer/thread already inserted
             * this eventId because of the UNIQUE constraint.
             *
             * Treat this as an idempotent duplicate.
             */
            if (repository.existsByEventId(eventId.toString())) {

                log.info(
                        "Event was already processed concurrently: eventId={}, eventType={}",
                        eventId,
                        event.getEventType()
                );

                return false;
            }

            log.error(
                    "Failed to mark event as processed: eventId={}, eventType={}",
                    eventId,
                    event.getEventType(),
                    ex
            );

            throw ex;
        }
    }


    private UUID getEventId(DomainEvent event) {

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
}