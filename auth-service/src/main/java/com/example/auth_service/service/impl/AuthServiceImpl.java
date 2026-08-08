package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.SessionResponse;
import com.example.auth_service.dto.session.SessionInfo;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.RoleName;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.RoleNotFoundException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.TokenManager;
import com.example.auth_service.service.UserSessionService;
import com.example.auth_service.session.SessionContext;
import com.example.auth_service.session.SessionContextExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

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

    @Override
    public AuthResponse register(RegisterRequest request, HttpServletRequest servletRequest) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        Role customerRole = roleRepository.findByRoleName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() ->
                        new RoleNotFoundException("ROLE_CUSTOMER not found"));

        SessionContext context = SessionContext.builder()
                .request(servletRequest)
                .build();

        SessionInfo sessionInfo = extractor.extract(context);

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

        return tokenManager.generateTokens(savedUser, sessionInfo);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest servletRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        SessionContext context = SessionContext.builder()
                        .request(servletRequest)
                        .build();

        SessionInfo sessionInfo = extractor.extract(context);
        return tokenManager.generateTokens(user, sessionInfo);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        return tokenManager.refreshAccessToken(refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        tokenManager.logout(refreshToken);
    }

    @Override
    public List<SessionResponse> getSessions() {
        User user = getCurrentUser();
        return userSessionService.getSessions(user);
    }

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