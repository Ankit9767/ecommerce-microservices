//package com.example.payment_service.scheduler;
//
//import com.ecommerce.common.events.PaymentCompletedEvent;
//import com.example.payment_service.entity.OutboxEvent;
//import com.example.payment_service.repository.OutboxRepository;
//import com.example.payment_service.kafka.PaymentCompletedProducer;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentOutboxScheduler {
//
//    private final OutboxRepository outboxRepository;
//
//    private final PaymentCompletedProducer kafkaProducer;
//
//    private final ObjectMapper objectMapper;
//
//    @Scheduled(fixedDelay = 5000)
//    public void publishEvents() {
//
//        List<OutboxEvent> events = outboxRepository.findByPublishedFalse();
//
//        for (OutboxEvent outbox : events) {
//
//            try {
//
//                PaymentCompletedEvent event = objectMapper.readValue(outbox.getPayload(), PaymentCompletedEvent.class);
//
//                kafkaProducer.publish(event);
//
//                outbox.setPublished(true);
//
//                outboxRepository.save(outbox);
//
//                log.info("Published Payment Outbox Event {}", outbox.getId());
//
//            } catch (Exception ex) {
//
//                log.error("Failed to publish Outbox Event {}", outbox.getId(), ex);
//
//            }
//
//        }
//
//    }
//
//}
