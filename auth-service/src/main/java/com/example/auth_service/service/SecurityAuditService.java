package com.example.auth_service.service;

import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.User;

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
}