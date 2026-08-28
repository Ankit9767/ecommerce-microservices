package com.example.notification_service.service.impl;

import com.example.notification_service.dto.NotificationProviderResponse;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.enums.NotificationStatus;
import com.example.notification_service.exception.NotificationNotFoundException;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {

    private final NotificationRepository repository;

    @Transactional
    public Notification createPending(Notification notification) {

        notification.setStatus(NotificationStatus.PENDING);

        return repository.saveAndFlush(notification);
    }

    @Transactional
    public Notification markProcessing(Long notificationId) {

        Notification notification = find(notificationId);

        notification.setStatus(NotificationStatus.PROCESSING);

        return repository.saveAndFlush(notification);
    }

    @Transactional
    public Notification markSent(Long notificationId,
                                 NotificationProviderResponse response) {

        Notification notification = find(notificationId);

        notification.setStatus(NotificationStatus.SENT);

        notification.setProviderReference(response.providerReference());

        notification.setFailureReason(null);

        notification.setSentAt(Instant.now());

        return repository.saveAndFlush(notification);
    }

    @Transactional
    public Notification markFailed(Long notificationId,
                                   NotificationProviderResponse response) {

        Notification notification = find(notificationId);

        notification.setStatus(NotificationStatus.FAILED);

        notification.setProviderReference(response.providerReference());

        notification.setFailureReason(response.failureReason());

        return repository.saveAndFlush(notification);
    }

    @Transactional(readOnly = true)
    public Notification find(Long notificationId) {

        return repository.findById(notificationId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                notificationId
                        )
                );
    }
}