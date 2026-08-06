package com.example.payment_service.service;

import com.ecommerce.common.dto.PaymentRequest;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.ecommerce.common.events.PaymentCompletedEvent;
import com.example.payment_service.entity.Payment;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.repository.PaymentRepository;
//import com.example.payment_service.kafka.PaymentCompletedProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final OutboxService outboxService;

    private final MeterRegistry meterRegistry;

//    private final PaymentCompletedProducer paymentCompletedProducer;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) throws JsonProcessingException {

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Payment payment = mapToEntity(request);

            payment.setStatus(PaymentStatus.SUCCESS);

            payment.setTransactionId(generateTransactionId());

            payment.setCreatedAt(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            Counter.builder("payments.success.total").description("Successful Payments").register(meterRegistry).increment();

            PaymentCompletedEvent event = PaymentCompletedEvent.builder().paymentId(savedPayment.getId()).orderId(savedPayment.getOrderId()).amount(savedPayment.getAmount()).paymentMethod(savedPayment.getPaymentMethod()).paymentStatus(savedPayment.getStatus()).transactionId(savedPayment.getTransactionId()).build();

            // using outbox pattern - saves event to DB, scheduler publishes to Kafka
            outboxService.savePaymentCompletedEvent(event);

//        using direct kafka publish
//        paymentCompletedProducer.publish(event);

            return mapToResponse(savedPayment);
        } finally {

            sample.stop(Timer.builder("order.processing.time").description("Order creation time").register(meterRegistry));
        }

    }

    @Override
    public PaymentResponse getPaymentById(Long id) {

        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));

        return mapToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    private String generateTransactionId() {

        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Payment mapToEntity(PaymentRequest request) {

        return Payment.builder().orderId(request.getOrderId()).amount(request.getAmount()).paymentMethod(request.getPaymentMethod()).build();
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return PaymentResponse.builder().paymentId(payment.getId()).orderId(payment.getOrderId()).amount(payment.getAmount()).paymentMethod(payment.getPaymentMethod()).status(payment.getStatus()).transactionId(payment.getTransactionId()).build();
    }

    @Override
    @Transactional
    public void processOrderEvent(OrderCreatedEvent event) throws JsonProcessingException {

        Optional<Payment> existingPayment = paymentRepository.findByOrderId(event.getOrderId());

        if (existingPayment.isPresent()) {

            log.warn("Payment already exists for Order {} ", event.getOrderId());

            return;
        }

        Payment payment = Payment.builder().orderId(event.getOrderId()).amount(event.getAmount()).paymentMethod(event.getPaymentMethod()).status(PaymentStatus.SUCCESS).transactionId(generateTransactionId()).createdAt(LocalDateTime.now()).build();

        Payment savedPayment = paymentRepository.save(payment);

        Counter.builder("kafka.order.events.consumed").description("Consumed OrderCreated events").register(meterRegistry).increment();

        PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder().paymentId(savedPayment.getId()).orderId(savedPayment.getOrderId()).amount(savedPayment.getAmount()).paymentMethod(savedPayment.getPaymentMethod()).paymentStatus(savedPayment.getStatus()).transactionId(savedPayment.getTransactionId()).build();

        // using outbox pattern - saves event to DB, scheduler publishes to Kafka
        outboxService.savePaymentCompletedEvent(completedEvent);

//        using direct kafka publish
//        paymentCompletedProducer.publish(completedEvent);

        log.info("Payment processed successfully for Order {}", event.getOrderId());

    }
}
