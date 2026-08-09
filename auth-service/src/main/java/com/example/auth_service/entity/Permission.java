package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_permission_name",
                        columnNames = "permission_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "permission_name",
            nullable = false,
            length = 100
    )
    private PermissionName permissionName;
}