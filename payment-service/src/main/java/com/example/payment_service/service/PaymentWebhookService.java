package com.example.payment_service.service;

import com.ecommerce.common.dto.PaymentResponse;
import com.example.payment_service.dto.webhook.PaymentWebhookRequest;

public interface PaymentWebhookService {

    PaymentResponse processWebhook(PaymentWebhookRequest request);
}