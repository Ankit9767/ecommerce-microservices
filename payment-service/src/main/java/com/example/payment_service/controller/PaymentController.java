package com.example.payment_service.controller;

import com.ecommerce.common.dto.PaymentRequest;
import com.ecommerce.common.dto.PaymentResponse;
import com.example.payment_service.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse processPayment(
            @Valid @RequestBody PaymentRequest request) throws JsonProcessingException {

        return paymentService.processPayment(request);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Long id) {

        return paymentService.getPaymentById(id);
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {

        return paymentService.getAllPayments();
    }

}