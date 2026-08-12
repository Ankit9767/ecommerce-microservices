package com.example.auth_service.entity;

import jakarta.persistence.*;
import com.ecommerce.common.entity.BaseEntity;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "blocked_ips",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blocked_ip_address",
                        columnNames = "ip_address"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedIp extends BaseEntity {

    @Column(
            name = "ip_address",
            nullable = false,
            length = 45
    )
    private String ipAddress;

    @Column(nullable = false)
    private Instant blockedUntil;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 255)
    private String reason;
}