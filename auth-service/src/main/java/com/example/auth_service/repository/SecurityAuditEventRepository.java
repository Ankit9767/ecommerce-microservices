package com.example.auth_service.repository;

import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.SecurityAuditEvent;
import com.example.auth_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, Long> {

    List<SecurityAuditEvent> findByUserOrderByEventTimeDesc(User user);

    List<SecurityAuditEvent> findByEventTypeOrderByEventTimeDesc(AuditEventType eventType);

    List<SecurityAuditEvent> findByUserAndEventTypeOrderByEventTimeDesc(User user,
                                                                        AuditEventType eventType);

    Page<SecurityAuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<SecurityAuditEvent> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<SecurityAuditEvent> findByEventTypeOrderByCreatedAtDesc(AuditEventType eventType,
                                                                 Pageable pageable);
}