package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.RoleName;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.RoleNotFoundException;
import com.example.auth_service.exception.UserAlreadyExistsException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        Role customerRole = roleRepository.findByRoleName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() ->
                        new RoleNotFoundException("ROLE_CUSTOMER not found"));

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .enabled(true)
                .build();

        user.getRoles().add(customerRole);

        userRepository.save(user);
    }

}