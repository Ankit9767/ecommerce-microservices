package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.AccountStatusRequest;
import com.example.auth_service.dto.request.ChangePasswordRequest;
import com.example.auth_service.dto.request.UpdateProfileRequest;
import com.example.auth_service.dto.response.RoleResponse;
import com.example.auth_service.dto.response.UserProfileResponse;
import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.InvalidPasswordException;
import com.example.auth_service.exception.PasswordMismatchException;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.SecurityAuditService;
import com.example.auth_service.service.UserService;
import com.example.auth_service.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserSessionService userSessionService;

    private final RoleRepository roleRepository;

    private final SecurityAuditService securityAuditService;

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

    @Override
    public void changePassword(ChangePasswordRequest request) {

        User user = getAuthenticatedUser();

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new InvalidPasswordException(
                    "Current password is incorrect"
            );
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new PasswordMismatchException(
                    "New password and confirmation password do not match"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password must be different from current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        userSessionService.revokeAllSessions(user);

        securityAuditService.record(
                AuditEventType.PASSWORD_CHANGED,
                user,
                user.getUsername(),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                "User changed password"
        );
    }

    @Override
    public void updateAccountStatus(Long userId, AccountStatusRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + userId
                        )
                );

        boolean enabled = request.getEnabled();

        user.setEnabled(enabled);

        userRepository.save(user);

        AuditEventType eventType =
                Boolean.TRUE.equals(user.getEnabled())
                        ? AuditEventType.ACCOUNT_ENABLED
                        : AuditEventType.ACCOUNT_DISABLED;

        securityAuditService.record(
                eventType,
                user,
                user.getUsername(),
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                Boolean.TRUE.equals(user.getEnabled())
                        ? "User account enabled"
                        : "User account disabled"
        );

        /*
         * If the account is disabled,
         * immediately revoke all refresh sessions.
         */
        if (!enabled) {

            userSessionService.revokeAllSessions(user);
        }
    }

    @Override
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + userId
                        )
                );

        /*
         * Revoke all refresh-token sessions first.
         */
        userSessionService.revokeAllSessions(user);

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );

        return user.getRoles()
                .stream()
                .map(role ->
                        RoleResponse.builder()
                                .id(role.getId())
                                .roleName(role.getRoleName())
                                .permissions(
                                        role.getPermissions()
                                                .stream()
                                                .map(permission ->
                                                        permission
                                                                .getPermissionName()
                                                                .name()
                                                )
                                                .collect(
                                                        java.util.stream.Collectors
                                                                .toSet()
                                                )
                                )
                                .build()
                )
                .toList();
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Long roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found: " + roleId
                        )
                );

        if (user.getRoles().contains(role)) {

            throw new IllegalStateException(
                    "User already has role: "
                            + role.getRoleName()
            );
        }

        user.getRoles().add(role);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeRole(Long userId, Long roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found: " + roleId
                        )
                );

        if (!user.getRoles().contains(role)) {

            throw new IllegalStateException("User does not have role: "
                            + role.getRoleName()
            );
        }

        /*
         * Don't allow a user to have zero roles.
         */
        if (user.getRoles().size() == 1) {

            throw new IllegalStateException(
                    "User must have at least one role"
            );
        }

        user.getRoles().remove(role);

        userRepository.save(user);
    }
}