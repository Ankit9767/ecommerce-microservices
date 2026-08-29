package com.example.notification_service.repository;

import com.example.notification_service.entity.NotificationProviderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationProviderTransactionRepository
        extends JpaRepository<NotificationProviderTransaction, Long> {

    Optional<NotificationProviderTransaction> findByNotificationId(Long notificationId);

    boolean existsByNotificationId(Long notificationId);
}