package com.example.auth_service.service;

import com.example.auth_service.dto.request.AccountStatusRequest;
import com.example.auth_service.dto.request.ChangePasswordRequest;
import com.example.auth_service.dto.request.UpdateProfileRequest;
import com.example.auth_service.dto.response.RoleResponse;
import com.example.auth_service.dto.response.UserProfileResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface UserService {

    @PreAuthorize("hasAuthority('USER_READ')")
    UserProfileResponse getCurrentUser();

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    UserProfileResponse updateCurrentUser(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    void updateAccountStatus(Long userId, AccountStatusRequest request);

    @PreAuthorize("hasAuthority('USER_DELETE')")
    void deleteUser(Long userId);

    @PreAuthorize("hasRole('ADMIN')")
    List<RoleResponse> getUserRoles(Long userId);

    @PreAuthorize("hasRole('ADMIN')")
    void assignRole(Long userId, Long roleId);

    @PreAuthorize("hasRole('ADMIN')")
    void removeRole(Long userId, Long roleId);
}