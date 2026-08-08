package com.example.auth_service.security.device;

public interface DeviceDetector {
    DeviceType detect(String userAgent);
}