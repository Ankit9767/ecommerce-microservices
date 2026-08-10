package com.example.auth_service.repository;

import com.example.auth_service.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    long countByUsernameAndSuccessfulFalseAndAttemptedAtAfter(String username, Instant after);

    long countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(String ipAddress, Instant after);

    List<LoginAttempt> findByUsernameAndAttemptedAtAfter(String username, Instant after);

    List<LoginAttempt> findByIpAddressAndAttemptedAtAfter(String ipAddress, Instant after);

    void deleteByUsernameAndSuccessfulFalse(String username);

    void deleteByIpAddressAndSuccessfulFalse(String ipAddress);
}