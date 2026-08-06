package com.example.payment_service.service;

import com.ecommerce.common.events.PaymentCompletedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface OutboxService {

    void savePaymentCompletedEvent(PaymentCompletedEvent event)
            throws JsonProcessingException;

}
