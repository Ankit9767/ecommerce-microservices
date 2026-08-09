package com.example.auth_service.controller;

import com.example.auth_service.dto.response.AuditResponse;
import com.example.auth_service.entity.AuditEventType;
import com.example.auth_service.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final SecurityAuditService securityAuditService;

    @GetMapping
    public Page<AuditResponse> getAll(Pageable pageable) {

        return securityAuditService.getAll(pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<AuditResponse> getByUser(@PathVariable Long userId,
            Pageable pageable) {

        return securityAuditService.getByUser(userId,
                pageable);
    }

    @GetMapping("/event/{eventType}")
    public Page<AuditResponse> getByEventType(@PathVariable AuditEventType eventType,
            Pageable pageable) {

        return securityAuditService.getByEventType(eventType,
                pageable);
    }
}