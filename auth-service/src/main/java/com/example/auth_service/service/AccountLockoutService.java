package com.example.auth_service.service;

import com.example.auth_service.entity.User;

public interface AccountLockoutService {

    boolean isLocked(User user);

    void registerFailedLogin(User user);

    void registerSuccessfulLogin(User user);

    void unlockIfExpired(User user);
}