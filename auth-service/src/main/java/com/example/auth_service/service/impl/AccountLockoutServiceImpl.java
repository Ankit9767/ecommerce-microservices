package com.example.auth_service.service.impl;

import com.example.auth_service.config.BruteForceProperties;
import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AccountLockoutService;
import com.example.auth_service.service.LoginAttemptService;
import com.example.auth_service.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountLockoutServiceImpl implements AccountLockoutService {

    private final UserRepository userRepository;

    private final LoginAttemptService loginAttemptService;

    private final BruteForceProperties bruteForceProperties;

    private final SecurityAuditService securityAuditService;

    @Override
    @Transactional(readOnly = true)
    public boolean isLocked(User user) {

        if (!Boolean.TRUE.equals(
                user.getAccountLocked()
        )) {
            return false;
        }

        if (user.getAccountLockedUntil() == null) {
            return true;
        }

        return Instant.now()
                .isBefore(
                        user.getAccountLockedUntil()
                );
    }

    @Override
    public void registerFailedLogin(User user) {

        if (Boolean.TRUE.equals(
                user.getAccountLocked()
        )) {
            return;
        }

        boolean limitExceeded =
                loginAttemptService
                        .hasExceededFailureLimit(
                                user.getUsername()
                        );

        if (!limitExceeded) {
            return;
        }

        user.setAccountLocked(true);

        user.setAccountLockedUntil(
                Instant.now()
                        .plus(
                                bruteForceProperties
                                        .getLockDuration()
                        )
        );

        userRepository.save(user);

        securityAuditService.record(
                AuditEventType.ACCOUNT_LOCKED,
                user,
                user.getUsername(),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "Account temporarily locked after repeated failed login attempts"
        );
    }

    @Override
    public void registerSuccessfulLogin(User user) {

        if (!Boolean.TRUE.equals(
                user.getAccountLocked()
        )) {
            return;
        }

        unlockIfExpired(user);
    }

    @Override
    public void unlockIfExpired(User user) {

        if (!Boolean.TRUE.equals(
                user.getAccountLocked()
        )) {
            return;
        }

        if (user.getAccountLockedUntil() == null) {
            return;
        }

        if (Instant.now().isBefore(
                user.getAccountLockedUntil()
        )) {
            return;
        }

        user.setAccountLocked(false);
        user.setAccountLockedUntil(null);

        userRepository.save(user);

        securityAuditService.record(
                AuditEventType.ACCOUNT_UNLOCKED,
                user,
                user.getUsername(),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "Account automatically unlocked after lockout expiration"
        );
    }
}