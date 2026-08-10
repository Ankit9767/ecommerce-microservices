package com.example.auth_service.service.impl;

import com.example.auth_service.config.BruteForceProperties;
import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.BlockedIp;
import com.example.auth_service.repository.BlockedIpRepository;
import com.example.auth_service.service.IpBlockService;
import com.example.auth_service.service.LoginAttemptService;
import com.example.auth_service.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class IpBlockServiceImpl
        implements IpBlockService {

    private final BlockedIpRepository blockedIpRepository;

    private final LoginAttemptService loginAttemptService;

    private final BruteForceProperties bruteForceProperties;

    private final SecurityAuditService securityAuditService;

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(String ipAddress) {

        return blockedIpRepository
                .findByIpAddressAndActiveTrue(ipAddress)
                .map(blockedIp -> {

                    if (blockedIp.getBlockedUntil() == null) {
                        return true;
                    }

                    return Instant.now()
                            .isBefore(
                                    blockedIp.getBlockedUntil()
                            );
                })
                .orElse(false);
    }

    @Override
    public void registerFailedAttempt(String ipAddress) {

        long failures =
                loginAttemptService
                        .countRecentFailuresByIp(ipAddress);

        if (failures <
                bruteForceProperties
                        .getMaxIpFailures()) {
            return;
        }

        BlockedIp blockedIp =
                blockedIpRepository
                        .findByIpAddress(ipAddress)
                        .orElse(
                                BlockedIp.builder()
                                        .ipAddress(ipAddress)
                                        .active(true)
                                        .build()
                        );

        blockedIp.setActive(true);

        blockedIp.setBlockedUntil(
                Instant.now()
                        .plus(
                                bruteForceProperties
                                        .getIpBlockDuration()
                        )
        );

        blockedIp.setReason("Repeated failed login attempts");

        blockedIpRepository.save(blockedIp);

        securityAuditService.record(
                AuditEventType.IP_BLOCKED,
                null,
                null,
                ipAddress,
                null,
                null,
                null,
                null,
                null,
                true,
                "IP temporarily blocked after repeated failed login attempts"
        );
    }

    @Override
    public void unblockIfExpired(String ipAddress) {

        blockedIpRepository
                .findByIpAddressAndActiveTrue(ipAddress)
                .ifPresent(blockedIp -> {

                    if (blockedIp.getBlockedUntil() != null
                            && !Instant.now().isBefore(
                            blockedIp.getBlockedUntil()
                    )) {

                        blockedIp.setActive(false);

                        blockedIpRepository.save(
                                blockedIp
                        );
                    }
                });
    }
}