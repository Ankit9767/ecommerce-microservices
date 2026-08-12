package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import com.ecommerce.common.entity.BaseEntity;

import java.time.Instant;

@Entity
@Table(
        name = "login_attempts",
        indexes = {
                @Index(
                        name = "idx_login_attempt_username",
                        columnList = "username"
                ),
                @Index(
                        name = "idx_login_attempt_ip",
                        columnList = "ip_address"
                ),
                @Index(
                        name = "idx_login_attempt_created",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String username;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private Boolean successful;

    @Column(nullable = false)
    private Instant attemptedAt;
}