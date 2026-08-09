package com.example.auth_service.service;

public interface PasswordResetService {

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword,
            String confirmPassword
    );
}