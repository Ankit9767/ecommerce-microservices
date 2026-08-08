package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.SessionResponse;
import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.entity.UserSession;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidRefreshTokenException;
import com.example.auth_service.exception.SessionNotFoundException;
import com.example.auth_service.exception.TokenReuseDetectedException;
import com.example.auth_service.repository.UserSessionRepository;
import com.example.auth_service.security.refresh.RefreshTokenGenerator;
import com.example.auth_service.security.refresh.TokenHashService;
import com.example.auth_service.security.jwt.JwtProperties;
import com.example.auth_service.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                .sessionId(
                        sessionInfo.getSessionId()
                )
                .tokenHash(hash)

                .expiryDate(Instant.now()
                            .plusMillis(
                                jwtProperties.getRefreshExpiration()))

                .revoked(false)

                .deviceName(sessionInfo.getDeviceName())

                .deviceType(sessionInfo.getDeviceType())

                .browser(sessionInfo.getBrowser())

                .operatingSystem(
                        sessionInfo.getOperatingSystem()
                )

                .ipAddress(sessionInfo.getIpAddress())

                .loginTime(Instant.now())

                .lastActivity(Instant.now())

                .user(user)

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

        UserSession session = userSessionRepository
                .findByTokenHash(hash)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token")
                );

        if (Boolean.TRUE.equals(session.getRevoked())) {
            throw new BadCredentialsException("Refresh token already revoked");
        }

        session.setRevoked(true);
        session.setLastActivity(Instant.now());

        userSessionRepository.save(session);
    }

    @Override
    public void revokeAllSessions(User user) {

        List<UserSession> sessions = userSessionRepository.findByUser(user);

        boolean hasActiveSession = sessions.stream()
                .anyMatch(session -> !Boolean.TRUE.equals(session.getRevoked()));

        if (!hasActiveSession) {
            throw new BadCredentialsException("All sessions already revoked");
        }

        sessions.forEach(session -> {
            session.setRevoked(true);
            session.setLastActivity(Instant.now());
        });

        userSessionRepository.saveAll(sessions);
    }

    @Override
    public List<SessionResponse> getSessions(User user,
            String currentSessionId) {

        Instant now = Instant.now();

        return userSessionRepository
                .findByUserAndRevokedFalse(user)
                .stream()

                .filter(session ->
                        session.getExpiryDate()
                                .isAfter(now)
                )

                .map(session ->
                        SessionResponse.builder()
                                .sessionId(
                                        session.getId()
                                )
                                .deviceName(
                                        session.getDeviceName()
                                )
                                .deviceType(
                                        session.getDeviceType()
                                )
                                .browser(
                                        session.getBrowser()
                                )
                                .operatingSystem(
                                        session.getOperatingSystem()
                                )
                                .ipAddress(
                                        session.getIpAddress()
                                )
                                .loginTime(
                                        session.getLoginTime()
                                )
                                .lastActivity(
                                        session.getLastActivity()
                                )
                                .expiryDate(
                                        session.getExpiryDate()
                                )
                                .currentSession(
                                        session.getSessionId()
                                                .equals(
                                                        currentSessionId
                                                )
                                )
                                .build()
                )
                .toList();
    }

    @Override
    public void revokeSession(Long sessionId, User user) {

        UserSession session = userSessionRepository
                .findByIdAndUser(sessionId, user)
                .orElseThrow(() ->
                        new SessionNotFoundException("Session not found")
                );

        session.setRevoked(true);
        userSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void updateLastActivity(String sessionId) {

        userSessionRepository
                .findBySessionId(sessionId)
                .ifPresent(session -> {

                    if (!Boolean.TRUE.equals(
                            session.getRevoked()
                    )
                            && !session.getExpiryDate()
                            .isBefore(Instant.now())) {

                        session.setLastActivity(
                                Instant.now()
                        );

                        userSessionRepository.save(
                                session
                        );
                    }
                });
    }

}