package com.example.order_service.scheduler;

import com.ecommerce.common.events.OrderCreatedEvent;
import com.example.order_service.Entity.OutboxEvent;
import com.example.order_service.Repository.OutboxRepository;
import com.example.order_service.kafka.OrderKafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;

    private final OrderKafkaProducer kafkaProducer;

    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events = outboxRepository.findByPublishedFalse();

        for (OutboxEvent outbox : events) {

            try {

                OrderCreatedEvent event = objectMapper.readValue(outbox.getPayload(), OrderCreatedEvent.class);

                kafkaProducer.publishOrderCreated(event);

                outbox.setPublished(true);

                outboxRepository.save(outbox);

                log.info("Published Outbox Event {}", outbox.getId());

            } catch (Exception ex) {

                log.error("Failed to publish Outbox Event {}", outbox.getId(), ex);

            }

        }

    }

}