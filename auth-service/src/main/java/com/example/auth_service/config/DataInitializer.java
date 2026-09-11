package com.example.auth_service.config;

import com.example.auth_service.entity.Permission;
import com.example.auth_service.entity.PermissionName;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.RoleName;
import com.example.auth_service.repository.PermissionRepository;
import com.example.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {

        initializePermissions();

        Role admin = getOrCreateRole(RoleName.ROLE_ADMIN);
        Role customer = getOrCreateRole(RoleName.ROLE_CUSTOMER);

        assignAdminPermissions(admin);
        assignCustomerPermissions(customer);
    }

    private void initializePermissions() {

        for (PermissionName permissionName : PermissionName.values()) {

            permissionRepository
                    .findByPermissionName(permissionName)
                    .orElseGet(() ->
                            permissionRepository.save(
                                    Permission.builder()
                                            .permissionName(permissionName)
                                            .build()
                            )
                    );
        }
    }

    private Role getOrCreateRole(RoleName roleName) {

        return roleRepository
                .findByRoleName(roleName)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .roleName(roleName)
                                        .build()
                        )
                );
    }

    private void assignAdminPermissions(Role role) {

        role.getPermissions().addAll(
                permissionRepository.findAll()
        );

        roleRepository.save(role);
    }

    private void assignCustomerPermissions(Role role) {

        assign(
                role,

                // Product
                PermissionName.PRODUCT_READ,

                // Search
                PermissionName.SEARCH_READ,

                // Cart
                PermissionName.CART_READ,
                PermissionName.CART_CREATE,
                PermissionName.CART_UPDATE,
                PermissionName.CART_DELETE,

                // Order
                PermissionName.ORDER_CREATE,
                PermissionName.ORDER_READ,
                PermissionName.ORDER_CANCEL,

                // Payment
                PermissionName.PAYMENT_CREATE,
                PermissionName.PAYMENT_READ,

                // Reviews
                PermissionName.REVIEW_READ,
                PermissionName.REVIEW_CREATE,
                PermissionName.REVIEW_UPDATE,
                PermissionName.REVIEW_DELETE,

                // Shipping
                PermissionName.SHIPMENT_READ,
                PermissionName.SHIPMENT_TRACK,

                // Notifications
                PermissionName.NOTIFICATION_READ,

                // Session
                PermissionName.SESSION_READ,
                PermissionName.SESSION_REVOKE,
                PermissionName.SESSION_REVOKE_ALL
        );
    }


    private void assign(Role role,
            PermissionName... permissionNames) {

        for (PermissionName permissionName : permissionNames) {

            Permission permission =
                    permissionRepository
                            .findByPermissionName(permissionName)
                            .orElseThrow();

            role.getPermissions().add(permission);
        }

        roleRepository.save(role);
    }
}
