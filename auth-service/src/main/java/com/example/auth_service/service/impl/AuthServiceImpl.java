package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.SessionResponse;
import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.RoleName;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.RoleNotFoundException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.jwt.JwtTokenValidator;
import com.example.auth_service.service.*;
import com.example.auth_service.session.SessionContext;
import com.example.auth_service.session.SessionContextExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final TokenManager tokenManager;

    private final UserSessionService userSessionService;

    private final SessionContextExtractor extractor;

    private final JwtTokenValidator jwtTokenValidator;

    private final SecurityAuditService securityAuditService;

    private final LoginAttemptService loginAttemptService;

    private final AccountLockoutService accountLockoutService;

    @Override
    public AuthResponse register(RegisterRequest request,
                                 HttpServletRequest servletRequest) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already exists"
            );
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username already exists"
            );
        }

        Role customerRole =
                roleRepository.findByRoleName(RoleName.ROLE_CUSTOMER)
                        .orElseThrow(() ->
                                new RoleNotFoundException(
                                        "ROLE_CUSTOMER not found"
                                )
                        );

        SessionContext context =
                SessionContext.builder()
                        .request(servletRequest)
                        .build();

        SessionInfo extractedInfo = extractor.extract(context);

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .build();

        user.getRoles().add(customerRole);

        User savedUser = userRepository.save(user);

        SessionInfo sessionInfo =
                SessionInfo.builder()
                        .sessionId(
                                UUID.randomUUID().toString()
                        )
                        .deviceName(
                                extractedInfo.getDeviceName()
                        )
                        .deviceType(
                                extractedInfo.getDeviceType()
                        )
                        .browser(
                                extractedInfo.getBrowser()
                        )
                        .operatingSystem(
                                extractedInfo.getOperatingSystem()
                        )
                        .ipAddress(
                                extractedInfo.getIpAddress()
                        )
                        .build();

        /*
         * TokenManager -> UserSessionService will create
         * the UserSession and record SESSION_CREATED.
         */
        AuthResponse response =
                tokenManager.generateTokens(
                        savedUser,
                        sessionInfo
                );

        /*
         * Registration automatically authenticates the user,
         * so record it as a successful authentication.
         */
        loginAttemptService.recordSuccess(
                request.getUsername(),
                extractedInfo.getIpAddress()
        );

        /*
         * Record successful authentication.
         */
        securityAuditService.record(
                AuditEventType.LOGIN_SUCCESS,
                savedUser,
                savedUser.getUsername(),
                extractedInfo.getIpAddress(),
                extractedInfo.getDeviceName(),
                extractedInfo.getDeviceType(),
                extractedInfo.getBrowser(),
                extractedInfo.getOperatingSystem(),
                sessionInfo.getSessionId(),
                true,
                "User registered and logged in successfully"
        );

        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request,
                              HttpServletRequest servletRequest) {

        SessionContext context =
                SessionContext.builder()
                        .request(servletRequest)
                        .build();

        SessionInfo extractedInfo = extractor.extract(context);

        User user = userRepository
                .findByUsername(request.getUsernameOrEmail())
                .or(() ->
                        userRepository.findByEmail(
                                request.getUsernameOrEmail()
                        )
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        accountLockoutService.unlockIfExpired(user);

        if (accountLockoutService.isLocked(user)) {
            throw new LockedException(
                    "Account is temporarily locked"
            );
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException ex) {

            loginAttemptService.recordFailure(
                    request.getUsernameOrEmail(),
                    extractedInfo.getIpAddress()
            );

            accountLockoutService.registerFailedLogin(user);

            securityAuditService.record(
                    AuditEventType.LOGIN_FAILED,
                    null,
                    request.getUsernameOrEmail(),
                    extractedInfo.getIpAddress(),
                    extractedInfo.getDeviceName(),
                    extractedInfo.getDeviceType(),
                    extractedInfo.getBrowser(),
                    extractedInfo.getOperatingSystem(),
                    null,
                    false,
                    "Login authentication failed"
            );

            throw ex;
        }

        SessionInfo sessionInfo =
                SessionInfo.builder()
                        .sessionId(UUID.randomUUID().toString())
                        .deviceName(
                                extractedInfo.getDeviceName()
                        )
                        .deviceType(
                                extractedInfo.getDeviceType()
                        )
                        .browser(
                                extractedInfo.getBrowser()
                        )
                        .operatingSystem(
                                extractedInfo.getOperatingSystem()
                        )
                        .ipAddress(
                                extractedInfo.getIpAddress()
                        )
                        .build();

        AuthResponse response =
                tokenManager.generateTokens(
                        user,
                        sessionInfo
                );

        loginAttemptService.recordSuccess(
                request.getUsernameOrEmail(),
                extractedInfo.getIpAddress()
        );

        accountLockoutService.registerSuccessfulLogin(user);

        securityAuditService.record(
                AuditEventType.LOGIN_SUCCESS,
                user,
                user.getUsername(),
                extractedInfo.getIpAddress(),
                extractedInfo.getDeviceName(),
                extractedInfo.getDeviceType(),
                extractedInfo.getBrowser(),
                extractedInfo.getOperatingSystem(),
                sessionInfo.getSessionId(),
                true,
                "User logged in successfully"
        );

        return response;
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        AuthResponse response = tokenManager.refreshAccessToken(refreshToken);

        securityAuditService.record(
                AuditEventType.TOKEN_REFRESH,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "Access token refreshed"
        );

        return response;
    }

    @PreAuthorize("@sessionSecurity.isOwner(#id, authentication.name)")
    @Override
    public void logout(String refreshToken) {

        tokenManager.logout(refreshToken);

        securityAuditService.record(
                AuditEventType.LOGOUT,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "User logged out"
        );
    }

    @Override
    public List<SessionResponse> getSessions(String accessToken) {
        User user = getCurrentUser();
        String sessionId =
                jwtTokenValidator.extractSessionId(
                        accessToken
                );
        return userSessionService.getSessions(user, sessionId);
    }

    @PreAuthorize("@sessionSecurity.isOwner(#id, authentication.name)")
    @Override
    public void logoutSession(Long id) {
        userSessionService.revokeSession(id, getCurrentUser());
    }

    @Override
    public void logoutAllSessions() {
        userSessionService.revokeAllSessions(getCurrentUser());
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        ));
    }

}