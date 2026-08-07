package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Boolean revoked;

    @Column(length = 100)
    private String deviceName;


    @Column(length = 100)
    private String browser;


    @Column(length = 100)
    private String operatingSystem;

    /*
     * Security information
     */

    @Column(length = 50)
    private String ipAddress;

    /*
     * Session lifecycle
     */

    @Column(nullable = false)
    private Instant loginTime;

    @Column(nullable = false)
    private Instant lastActivity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}