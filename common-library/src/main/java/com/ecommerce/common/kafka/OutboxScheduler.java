package com.ecommerce.common.kafka;

import com.ecommerce.common.events.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Single shared transactional-outbox drainer used by every service. It reads
 * unpublished rows from the common-library {@link OutboxRepository} and
 * dispatches each to the {@link OutboxProducer} that {@link
 * OutboxProducer#supports(EventType) supports} the row's event type. The
 * concrete event is reconstructed from the stored JSON via {@link
 * EventType#getEventClass()}, so no per-service deserialization switch is
 * needed.
 *
 * <p>Exists only in services that register at least one {@link OutboxProducer}
 * bean (otherwise there is nothing to publish).</p>
 */
@Slf4j
@Component
@Profile("!test")
@ConditionalOnBean(OutboxProducer.class)
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository repository;

    private final List<OutboxProducer> producers;

    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events =
                repository.findTop100ByPublishedFalseOrderByIdAsc();

        for (OutboxEvent outbox : events) {

            try {

                EventType type = EventType.fromValue(outbox.getEventType());

                if (type == null) {

                    log.warn(
                            "Skipping outbox event {} with unknown type '{}'",
                            outbox.getId(),
                            outbox.getEventType()
                    );

                    continue;
                }

                OutboxProducer producer =
                        producers.stream()
                                .filter(p -> p.supports(type))
                                .findFirst()
                                .orElse(null);

                if (producer == null) {

                    log.warn(
                            "No producer registered for event type {} " +
                                    "(outbox={})",
                            type,
                            outbox.getId()
                    );

                    continue;
                }

                DomainEvent event =
                        objectMapper.readValue(
                                outbox.getPayload(),
                                type.getEventClass()
                        );

                /*
                 * IMPORTANT:
                 *
                 * Wait for Kafka acknowledgement.
                 */
                producer.publish(event).get();

                /*
                 * Kafka confirmed successful publication.
                 *
                 * Only NOW mark the outbox event as published.
                 */
                outbox.setPublished(true);

                repository.save(outbox);

                log.info(
                        "Successfully published outbox event id={}, type={}",
                        outbox.getId(),
                        type
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to publish outbox event id={}",
                        outbox.getId(),
                        ex
                );
            }
        }
    }
}
