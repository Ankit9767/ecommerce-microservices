package com.example.auth_service.controller;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RefreshTokenRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.AuthResponse;
import com.example.auth_service.dto.response.SessionResponse;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request, servletRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok(
                authService.login(request, servletRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid
            @RequestBody
            RefreshTokenRequest request) {
        return ResponseEntity.ok(
                authService.refreshToken(
                        request.getRefreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid
            @RequestBody
            RefreshTokenRequest request) {
        authService.logout(
                request.getRefreshToken());
        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getSessions() {
        return ResponseEntity.ok(authService.getSessions());
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<String> logoutSession(@PathVariable Long id) {

        authService.logoutSession(id);
        return ResponseEntity.ok("Session revoked");
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<String> logoutAllSessions() {

        authService.logoutAllSessions();
        return ResponseEntity.ok("All sessions revoked");
    }

}