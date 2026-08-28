package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationProviderRequest;
import com.example.notification_service.dto.NotificationProviderResponse;

public interface NotificationProvider {

    String getProviderName();

    NotificationProviderResponse send(NotificationProviderRequest request);
}