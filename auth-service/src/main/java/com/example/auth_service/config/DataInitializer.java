package com.example.auth_service.config;


import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.RoleName;
import com.example.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        if (roleRepository.findByRoleName(RoleName.ROLE_ADMIN).isEmpty()) {

            roleRepository.save(
                    Role.builder()
                            .roleName(RoleName.ROLE_ADMIN)
                            .build()
            );

        }

        if (roleRepository.findByRoleName(RoleName.ROLE_CUSTOMER).isEmpty()) {

            roleRepository.save(
                    Role.builder()
                            .roleName(RoleName.ROLE_CUSTOMER)
                            .build()
            );

        }

    }

}
