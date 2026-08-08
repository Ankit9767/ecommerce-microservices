package com.example.auth_service.repository;

import com.example.auth_service.entity.User;
import com.example.auth_service.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByTokenHash(String tokenHash);

    List<UserSession> findByUser(User user);

    Optional<UserSession> findByIdAndUser(Long id, User user);

    List<UserSession> findByUserAndRevokedFalse(User user);

    Optional<UserSession> findBySessionId(String sessionId);

}