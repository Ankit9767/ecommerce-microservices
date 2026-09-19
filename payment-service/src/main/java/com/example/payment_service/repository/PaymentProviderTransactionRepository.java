package com.example.payment_service.repository;

import com.example.payment_service.entity.PaymentProviderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentProviderTransactionRepository
        extends JpaRepository<PaymentProviderTransaction, Long> {

    Optional<PaymentProviderTransaction> findByPaymentId(Long paymentId);

    Optional<PaymentProviderTransaction> findByProviderOrderId(String providerOrderId);

    Optional<PaymentProviderTransaction> findByProviderPaymentId(String providerPaymentId);

    Optional<PaymentProviderTransaction> findByProviderReference(String providerReference);
}