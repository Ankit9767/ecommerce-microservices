package com.example.auth_service.service;

public interface LoginAttemptService {

    void recordSuccess(String username, String ipAddress);

    void recordFailure(String username, String ipAddress);

    long countRecentFailuresByUsername(String username);

    long countRecentFailuresByIp(String ipAddress);

    boolean hasExceededFailureLimit(String username);
}