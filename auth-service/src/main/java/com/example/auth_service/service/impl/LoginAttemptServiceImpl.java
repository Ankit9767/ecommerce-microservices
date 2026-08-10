package com.example.auth_service.service.impl;

import com.example.auth_service.entity.LoginAttempt;
import com.example.auth_service.repository.LoginAttemptRepository;
import com.example.auth_service.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(15);

    @Override
    public void recordSuccess(String username, String ipAddress) {

        LoginAttempt attempt =
                LoginAttempt.builder()
                        .username(username)
                        .ipAddress(ipAddress)
                        .successful(true)
                        .attemptedAt(Instant.now())
                        .build();

        loginAttemptRepository.save(attempt);
    }

    @Override
    public void recordFailure(String username, String ipAddress) {

        LoginAttempt attempt =
                LoginAttempt.builder()
                        .username(username)
                        .ipAddress(ipAddress)
                        .successful(false)
                        .attemptedAt(Instant.now())
                        .build();

        loginAttemptRepository.save(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRecentFailuresByUsername(String username) {

        Instant after = Instant.now().minus(FAILURE_WINDOW);

        return loginAttemptRepository
                .countByUsernameAndSuccessfulFalseAndAttemptedAtAfter(
                        username,
                        after
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countRecentFailuresByIp(String ipAddress) {

        Instant after = Instant.now().minus(FAILURE_WINDOW);

        return loginAttemptRepository
                .countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(
                        ipAddress,
                        after
                );
    }
}