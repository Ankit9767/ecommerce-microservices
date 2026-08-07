package com.example.auth_service.service.impl;

import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.entity.UserSession;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidRefreshTokenException;
import com.example.auth_service.exception.TokenReuseDetectedException;
import com.example.auth_service.repository.UserSessionRepository;
import com.example.auth_service.security.refresh.RefreshTokenGenerator;
import com.example.auth_service.security.refresh.TokenHashService;
import com.example.auth_service.security.jwt.JwtProperties;
import com.example.auth_service.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl
        implements UserSessionService {

    private final UserSessionRepository userSessionRepository;

    private final JwtProperties jwtProperties;

    private final TokenHashService tokenHashService;

    private final RefreshTokenGenerator refreshTokenGenerator;

    @Override
    public String createSession(User user, SessionInfo sessionInfo) {

        String token = refreshTokenGenerator.generate();

        String hash = tokenHashService.hash(token);

        UserSession session =
                UserSession.builder()
                        .tokenHash(hash)
                        .user(user)
                        .expiryDate(
                                Instant.now()
                                        .plusMillis(
                                                jwtProperties.getRefreshExpiration())
                        )
                        .revoked(false)
                        .loginTime(
                                Instant.now()
                        )
                        .lastActivity(
                                Instant.now()
                        )
                        .deviceName(
                                sessionInfo.getDeviceName()
                        )
                        .browser(
                                sessionInfo.getBrowser()
                        )
                        .operatingSystem(
                                sessionInfo.getOperatingSystem()
                        )
                        .ipAddress(
                                sessionInfo.getIpAddress()
                        )
                        .build();

        userSessionRepository.save(session);
        return token;
    }

    @Override
    public UserSession verifySession(String token) {

        String hash = tokenHashService.hash(token);

        UserSession session = userSessionRepository.findByTokenHash(hash)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid Refresh Token"
                                ));

        if (session.getRevoked()) {
            revokeAllSessions(session.getUser());
            throw new TokenReuseDetectedException(
                    "Refresh token reuse detected"
            );
        }

        if (session.getExpiryDate().isBefore(Instant.now())) {
            userSessionRepository.delete(session);
            throw new InvalidRefreshTokenException(
                    "Refresh token expired"
            );
        }

        session.setLastActivity(Instant.now());
        userSessionRepository.save(session);

        return session;
    }

    @Override
    public void revokeSession(String token) {

        String hash = tokenHashService.hash(token);

        userSessionRepository.findByTokenHash(hash)
                .ifPresent(session -> {
                    session.setRevoked(true);
                    userSessionRepository.save(session);
                });

    }

    @Override
    public void revokeAllSessions(User user) {

        List<UserSession> tokens = userSessionRepository.findByUser(user);
        tokens.forEach(token ->
                token.setRevoked(true)
        );
        userSessionRepository.saveAll(tokens);

    }

}