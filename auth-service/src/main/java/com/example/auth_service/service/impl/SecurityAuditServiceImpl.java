package com.example.auth_service.service.impl;

import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.SecurityAuditEvent;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.SecurityAuditEventRepository;
import com.example.auth_service.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final SecurityAuditEventRepository auditEventRepository;

    @Override
    public void record(
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
    ) {

        SecurityAuditEvent event =
                SecurityAuditEvent.builder()

                        .eventType(eventType)

                        .user(user)

                        .username(username)

                        .ipAddress(ipAddress)

                        .deviceName(deviceName)

                        .deviceType(deviceType)

                        .browser(browser)

                        .operatingSystem(operatingSystem)

                        .sessionId(sessionId)

                        .eventTime(Instant.now())

                        .successful(successful)

                        .description(description)

                        .build();

        auditEventRepository.save(event);
    }
}