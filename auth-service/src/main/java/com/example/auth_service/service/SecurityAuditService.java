package com.example.auth_service.service;

import com.example.auth_service.dto.response.AuditResponse;
import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SecurityAuditService {

    void record(
            AuditEventType eventType,
            User user,
            String username,
            String ipAddress,
            String deviceName,
            String deviceType,
            String browser,
            String operatingSystem,
            String sessionId,
            boolean successful,
            String description
    );

    Page<AuditResponse> getAll(Pageable pageable);

    Page<AuditResponse> getByUser(Long userId, Pageable pageable);

    Page<AuditResponse> getByEventType(AuditEventType eventType, Pageable pageable);

}