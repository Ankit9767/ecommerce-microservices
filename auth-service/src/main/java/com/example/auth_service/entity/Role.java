package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role_name",
            nullable = false,
            unique = true,
            length = 50
    )
    private RoleName roleName;
}