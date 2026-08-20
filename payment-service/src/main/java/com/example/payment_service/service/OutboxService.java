package com.example.payment_service.service;

import com.ecommerce.common.events.PaymentEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface OutboxService {

    void savePaymentCompletedEvent(PaymentEvent event)
            throws JsonProcessingException;

}