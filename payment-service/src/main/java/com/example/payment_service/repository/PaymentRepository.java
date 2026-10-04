package com.example.payment_service.repository;

import com.example.payment_service.entity.Payment;
import com.ecommerce.common.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
            Long orderId, PaymentStatus status);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);

    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Optional<Payment> findByProviderReference(String providerReference);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    Optional<Payment> findByProviderOrderId(String providerOrderId);

    Optional<Payment> findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(
            Long orderId, List<PaymentStatus> statuses);

}