package com.example.payment_service.service;

import com.ecommerce.common.events.PaymentEvent;

public interface OutboxService {

    void savePaymentCompletedEvent(PaymentEvent event);
}