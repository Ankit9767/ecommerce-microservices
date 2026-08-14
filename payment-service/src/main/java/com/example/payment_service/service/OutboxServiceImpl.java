//package com.example.payment_service.service;
//
//import com.ecommerce.common.events.PaymentCompletedEvent;
//import com.example.payment_service.entity.OutboxEvent;
//import com.example.payment_service.repository.OutboxRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class OutboxServiceImpl
//        implements OutboxService {
//
//    private final OutboxRepository repository;
//
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void savePaymentCompletedEvent(PaymentCompletedEvent event)
//            throws JsonProcessingException {
//
//        OutboxEvent outbox = OutboxEvent.builder()
//                .eventType("PAYMENT_COMPLETED")
//                .aggregateId(event.getOrderId())
//                .payload(objectMapper.writeValueAsString(event))
//                .published(false)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        repository.save(outbox);
//
//    }
//}
