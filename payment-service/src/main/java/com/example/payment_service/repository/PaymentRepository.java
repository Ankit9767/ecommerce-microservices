package com.example.payment_service.repository;

import com.example.payment_service.entity.Payment;
import com.ecommerce.common.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Optional<Payment> findByProviderReference(String providerReference);

    boolean existsByOrderId(Long orderId);
}