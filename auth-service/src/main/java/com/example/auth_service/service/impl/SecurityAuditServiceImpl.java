package com.example.auth_service.service.impl;

import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.SecurityAuditEvent;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.SecurityAuditEventRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.auth_service.dto.response.AuditResponse;


import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final SecurityAuditEventRepository auditEventRepository;

    private final UserRepository userRepository;

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

    @Override
    public Page<AuditResponse> getAll(Pageable pageable) {

        return auditEventRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<AuditResponse> getByUser(Long userId, Pageable pageable) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + userId
                        )
                );

        return auditEventRepository
                .findByUserOrderByCreatedAtDesc(
                        user,
                        pageable
                )
                .map(this::toResponse);
    }

    @Override
    public Page<AuditResponse> getByEventType(AuditEventType eventType,
                                              Pageable pageable) {

        return auditEventRepository
                .findByEventTypeOrderByCreatedAtDesc(
                        eventType,
                        pageable
                )
                .map(this::toResponse);
    }

    private AuditResponse toResponse(SecurityAuditEvent audit) {

        User user = audit.getUser();

        return AuditResponse.builder()
                .id(audit.getId())
                .userId(
                        user != null
                                ? user.getId()
                                : null
                )
                .username(audit.getUsername())
                .eventType(audit.getEventType())
                .ipAddress(audit.getIpAddress())
                .deviceName(audit.getDeviceName())
                .deviceType(audit.getDeviceType())
                .browser(audit.getBrowser())
                .operatingSystem(
                        audit.getOperatingSystem()
                )
                .sessionId(audit.getSessionId())
                .successful(audit.getSuccessful())
                .description(audit.getDescription())
                .createdAt(audit.getCreatedAt())
                .build();
    }
}