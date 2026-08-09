package com.example.auth_service.service;

import com.example.auth_service.dto.request.AccountStatusRequest;
import com.example.auth_service.dto.request.ChangePasswordRequest;
import com.example.auth_service.dto.request.UpdateProfileRequest;
import com.example.auth_service.dto.response.RoleResponse;
import com.example.auth_service.dto.response.UserProfileResponse;

import java.util.List;

public interface UserService {

    UserProfileResponse getCurrentUser();

    UserProfileResponse updateCurrentUser(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void updateAccountStatus(Long userId, AccountStatusRequest request);

    void deleteUser(Long userId);

    List<RoleResponse> getUserRoles(Long userId);

    void assignRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);
}