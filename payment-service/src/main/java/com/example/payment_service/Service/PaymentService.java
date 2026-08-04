package com.example.payment_service.Service;

import com.ecommerce.common.dto.PaymentRequest;
import com.ecommerce.common.dto.PaymentResponse;
import com.ecommerce.common.events.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request) throws JsonProcessingException;

    PaymentResponse getPaymentById(Long id);

    List<PaymentResponse> getAllPayments();

    void processOrderEvent(OrderCreatedEvent event) throws JsonProcessingException;
}
