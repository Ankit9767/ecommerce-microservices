package com.example.auth_service.service;

public interface IpBlockService {

    boolean isBlocked(String ipAddress);

    void registerFailedAttempt(String ipAddress);

    void unblockIfExpired(String ipAddress);
}