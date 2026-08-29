package com.example.notification_service.service.impl;

import com.example.notification_service.dto.NotificationProviderRequest;
import com.example.notification_service.dto.NotificationProviderResponse;
import com.example.notification_service.entity.NotificationProviderTransaction;
import com.example.notification_service.enums.NotificationStatus;
import com.example.notification_service.exception.*;
import com.example.notification_service.repository.NotificationProviderTransactionRepository;
import com.example.notification_service.service.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockNotificationProvider implements NotificationProvider {

    private final NotificationProviderTransactionRepository repository;

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    @Transactional
    public NotificationProviderResponse send(NotificationProviderRequest request) {

        validateRequest(request);

        Long notificationId = request.notificationId();

        /*
         * notificationId is the idempotency key.
         *
         */
        NotificationProviderTransaction existing = findExisting(notificationId);

        if (existing != null) {

            log.info(
                    "Returning existing notification provider transaction: " +
                            "notificationId={}, providerReference={}, status={}",
                    notificationId,
                    existing.getProviderReference(),
                    existing.getStatus()
            );

            return toResponse(existing);
        }

        NotificationProviderTransaction transaction =
                NotificationProviderTransaction.builder()
                        .notificationId(notificationId)
                        .providerReference(generateProviderReference())
                        .status(NotificationStatus.SENT)
                        .createdAt(Instant.now())
                        .build();

        try {

            NotificationProviderTransaction saved =
                    repository.saveAndFlush(transaction);

            log.info(
                    "Created new notification provider transaction: " +
                            "notificationId={}, providerReference={}",
                    notificationId,
                    saved.getProviderReference()
            );

            return toResponse(saved);

        } catch (DataIntegrityViolationException ex) {

            /*
             * Request A:
             *     INSERT succeeds
             *
             * Request B:
             *     INSERT fails because notification_id is UNIQUE
             *
             * Request B must return A's transaction instead of
             * generating another provider transaction.
             */
            NotificationProviderTransaction concurrent =
                    findExisting(notificationId);

            if (concurrent != null) {

                log.info(
                        "Notification provider transaction was created " +
                                "concurrently. Returning existing transaction: " +
                                "notificationId={}, providerReference={}",
                        notificationId,
                        concurrent.getProviderReference()
                );

                return toResponse(concurrent);
            }

            log.error(
                    "Unable to create notification provider transaction: " +
                            "notificationId={}",
                    notificationId,
                    ex
            );

            throw new NotificationProviderException(
                    "Unable to create notification provider transaction " +
                            "for notificationId=" + notificationId,
                    ex
            );

        } catch (DataAccessException ex) {

            log.error(
                    "Notification provider database error: " +
                            "notificationId={}",
                    notificationId,
                    ex
            );

            throw new NotificationProviderException(
                    "Notification provider database failure for " +
                            "notificationId=" + notificationId,
                    ex
            );
        }
    }

    private NotificationProviderTransaction findExisting(Long notificationId) {

        try {

            return repository
                    .findByNotificationId(notificationId)
                    .orElse(null);

        } catch (DataAccessException ex) {

            throw new NotificationProviderException(
                    "Unable to check notification provider transaction " +
                            "for notificationId=" + notificationId,
                    ex
            );
        }
    }

    private NotificationProviderResponse toResponse(
            NotificationProviderTransaction transaction) {

        return new NotificationProviderResponse(
                transaction.getProviderReference(),
                transaction.getStatus() == NotificationStatus.SENT,
                null
        );
    }

    private String generateProviderReference() {

        return "NOTIFY-" + UUID.randomUUID();
    }

    private void validateRequest(NotificationProviderRequest request) {

        if (request == null) {
            throw new InvalidNotificationProviderRequestException();
        }

        if (request.notificationId() == null) {
            throw new MissingNotificationIdException();
        }

        if (request.recipient() == null ||
                request.recipient().isBlank()) {
            throw new InvalidNotificationRecipientException();
        }

        if (request.subject() == null ||
                request.subject().isBlank()) {
            throw new InvalidNotificationSubjectException();
        }

        if (request.message() == null ||
                request.message().isBlank()) {
            throw new InvalidNotificationMessageException();
        }
    }

}