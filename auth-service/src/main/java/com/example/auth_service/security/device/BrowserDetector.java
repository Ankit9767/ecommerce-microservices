package com.example.auth_service.security.device;

public interface BrowserDetector {
    BrowserInfo detect(String userAgent);
}