package com.example.payment_service.service;

public interface PaymentWebhookService {

    void processRazorpayWebhook(String rawBody, String eventId);
}