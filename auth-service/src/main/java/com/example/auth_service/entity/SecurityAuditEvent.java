package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "security_audit_events",
        indexes = {

                @Index(
                        name = "idx_audit_user",
                        columnList = "user_id"
                ),

                @Index(
                        name = "idx_audit_event_type",
                        columnList = "event_type"
                ),

                @Index(
                        name = "idx_audit_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditEvent extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 50
    )
    private AuditEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String username;

    @Column(length = 255)
    private String ipAddress;

    @Column(length = 100)
    private String deviceName;

    @Column(length = 100)
    private String deviceType;

    @Column(length = 100)
    private String browser;

    @Column(length = 100)
    private String operatingSystem;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean successful;

    @Column(length = 100)
    private String sessionId;
}