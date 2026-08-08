package com.example.auth_service.security.device;

public interface OperatingSystemDetector {
    OperatingSystemInfo detect(String userAgent);
}