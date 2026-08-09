package com.example.auth_service.dto.response;

import com.example.auth_service.entity.AuditEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
public class AuditResponse {

    private Long id;

    private Long userId;

    private String username;

    private AuditEventType eventType;

    private String ipAddress;

    private String deviceName;

    private String deviceType;

    private String browser;

    private String operatingSystem;

    private String sessionId;

    private Boolean successful;

    private String description;

    private LocalDateTime createdAt;
}