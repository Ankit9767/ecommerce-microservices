package com.example.notification_service.service.impl;

import com.example.notification_service.dto.NotificationProviderRequest;
import com.example.notification_service.dto.NotificationProviderResponse;
import com.example.notification_service.service.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MockNotificationProvider implements NotificationProvider {

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public NotificationProviderResponse send(NotificationProviderRequest request) {

        log.info(
                "Sending notification through MOCK provider: " +
                        "notificationId={}, recipient={}, subject={}",
                request.notificationId(),
                request.recipient(),
                request.subject()
        );

        String reference = "NOTIFY-" + UUID.randomUUID();

        return new NotificationProviderResponse(
                reference,
                true,
                null
        );
    }
}