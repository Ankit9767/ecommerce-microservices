package com.example.auth_service.service.impl;

import com.example.auth_service.entity.PasswordResetToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidPasswordException;
import com.example.auth_service.exception.PasswordMismatchException;
import com.example.auth_service.repository.PasswordResetTokenRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.PasswordResetService;
import com.example.auth_service.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetServiceImpl
        implements PasswordResetService {

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserSessionService userSessionService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Override
    public void requestPasswordReset(String email) {

        userRepository.findByEmail(email)
                .ifPresent(this::createResetToken);
    }

    private void createResetToken(User user) {

        /*
         * Remove previous reset tokens for this user.
         */
        tokenRepository.deleteByUser(user);

        /*
         * Generate cryptographically secure random token.
         */
        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        /*
         * Store only the hash.
         */
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .tokenHash(tokenHash)
                        .expiresAt(
                                Instant.now()
                                        .plus(15, ChronoUnit.MINUTES)
                        )
                        .used(false)
                        .user(user)
                        .build();

        tokenRepository.save(resetToken);

        /*
         * For now we print the raw token.
         *
         * Later this will be replaced by
         * an email notification service.
         */
        System.out.println("PASSWORD RESET TOKEN: " + rawToken);
    }

    @Override
    public void resetPassword(String token, String newPassword,
            String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {

            throw new PasswordMismatchException(
                    "New password and confirmation password do not match"
            );
        }

        String tokenHash = hashToken(token);

        PasswordResetToken resetToken =
                tokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new InvalidPasswordException(
                                        "Invalid or expired reset token"
                                )
                        );

        if (Boolean.TRUE.equals(resetToken.getUsed())) {

            throw new InvalidPasswordException(
                    "Reset token has already been used"
            );
        }

        if (resetToken.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new InvalidPasswordException(
                    "Reset token has expired"
            );
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password must be different from current password"
            );
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        /*
         * Password reset invalidates
         * all refresh-token sessions.
         */
        userSessionService.revokeAllSessions(user);

        /*
         * Make token single-use.
         */
        resetToken.setUsed(true);

        tokenRepository.save(resetToken);
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }
}