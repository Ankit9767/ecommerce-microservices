package com.example.payment_service.controller;

import com.ecommerce.common.dto.PaymentResponse;
import com.example.payment_service.dto.webhook.PaymentWebhookRequest;
import com.example.payment_service.service.PaymentWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentWebhookService webhookService;

    @PostMapping("/webhook")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<PaymentResponse> handleWebhook(
            @Valid @RequestBody PaymentWebhookRequest request) {

        return ResponseEntity.ok(
                webhookService.processWebhook(request)
        );
    }
}