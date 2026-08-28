package com.example.notification_service.repository;

import com.example.notification_service.entity.Notification;
import com.example.notification_service.enums.NotificationStatus;
import com.example.notification_service.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );

    List<Notification> findByOrderIdOrderByCreatedAtDesc(
            Long orderId
    );

    Optional<Notification> findByOrderIdAndType(
            Long orderId,
            NotificationType type
    );

    List<Notification> findByStatus(
            NotificationStatus status
    );
}