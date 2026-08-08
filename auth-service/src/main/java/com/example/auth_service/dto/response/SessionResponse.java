package com.example.auth_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class SessionResponse {

    private Long sessionId;

    private String deviceName;

    private String browser;

    private String operatingSystem;

    private String ipAddress;

    private Instant loginTime;

    private Instant lastActivity;

    private Instant expiryDate;

    private boolean currentSession;

}