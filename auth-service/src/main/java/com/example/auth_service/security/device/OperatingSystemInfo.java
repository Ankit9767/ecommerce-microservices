package com.example.auth_service.security.device;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OperatingSystemInfo {

    private String operatingSystem;

    private String version;
}