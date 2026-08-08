package com.example.auth_service.dto.session;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionInfo {

    private String sessionId;

    private String deviceName;

    private String deviceType;

    private String browser;

    private String operatingSystem;

    private String ipAddress;

}