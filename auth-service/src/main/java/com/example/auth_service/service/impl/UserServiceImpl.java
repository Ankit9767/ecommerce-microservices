package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.UpdateProfileRequest;
import com.example.auth_service.dto.response.UserProfileResponse;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser() {

        User user = getAuthenticatedUser();

        return mapToResponse(user);
    }

    @Override
    public UserProfileResponse updateCurrentUser(UpdateProfileRequest request) {

        User user = getAuthenticatedUser();

        user.setFirstName(request.getFirstName());

        user.setLastName(request.getLastName());

        user.setPhone(request.getPhone());

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new UsernameNotFoundException(
                    "Authenticated user not found"
            );
        }

        String username =authentication.getName();

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + username
                        )
                );
    }

    private UserProfileResponse mapToResponse(User user) {

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}