package com.example.auth_service.repository;

import com.example.auth_service.entity.BlockedIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockedIpRepository extends JpaRepository<BlockedIp, Long> {

    Optional<BlockedIp> findByIpAddress(String ipAddress);

    Optional<BlockedIp> findByIpAddressAndActiveTrue(String ipAddress);
}