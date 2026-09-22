package com.example.payment_service.service.provider;

import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.entity.PaymentProviderTransaction;
import com.example.payment_service.exception.InvalidPaymentProviderRequestException;
import com.example.payment_service.exception.PaymentProviderException;
import com.example.payment_service.repository.PaymentProviderTransactionRepository;
import com.example.payment_service.service.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "payment.provider",
        havingValue = "MOCK",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class MockPaymentProvider implements PaymentProvider {

    private final PaymentProviderTransactionRepository repository;

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    @Transactional
    public PaymentProviderResponse createProviderPaymentTransaction(PaymentProviderRequest request) {

        validateRequest(request);

        Long paymentId = request.paymentId();

        /*
         * paymentId is the provider idempotency key.
         */
        PaymentProviderTransaction existing = findExisting(paymentId);

        if (existing != null) {

            log.info(
                    "Returning existing provider payment: " +
                            "paymentId={}, providerReference={}, status={}",
                    paymentId,
                    existing.getProviderReference(),
                    existing.getStatus()
            );

            return toResponse(existing);
        }

        String providerOrderId = "MOCK-ORDER-" + UUID.randomUUID();

        String providerPaymentId = "MOCK-PAYMENT-" + UUID.randomUUID();

        String providerReference = providerPaymentId;

        PaymentProviderTransaction transaction =
                PaymentProviderTransaction.builder()
                        .paymentId(paymentId)
                        .providerOrderId(providerOrderId)
                        .providerPaymentId(providerPaymentId)
                        .providerReference(providerReference)
                        .status(PaymentStatus.PENDING)
                        .build();

        try {

            PaymentProviderTransaction saved =
                    repository.saveAndFlush(transaction);

            log.info(
                    "Created new provider payment: " +
                            "paymentId={}, providerReference={}",
                    paymentId,
                    saved.getProviderReference()
            );

            return toResponse(saved);

        } catch (DataIntegrityViolationException ex) {

            /*
             * Another request may have created the provider
             * transaction concurrently.
             */
            PaymentProviderTransaction concurrent = findExisting(paymentId);

            if (concurrent != null) {

                log.info(
                        "Provider payment created concurrently. " +
                                "Returning existing transaction: " +
                                "paymentId={}, providerReference={}",
                        paymentId,
                        concurrent.getProviderReference()
                );

                return toResponse(concurrent);
            }

            log.error(
                    "Unable to create provider payment: paymentId={}",
                    paymentId,
                    ex
            );

            throw new PaymentProviderException(
                    "Unable to create provider payment for paymentId="
                            + paymentId,
                    ex
            );

        } catch (DataAccessException ex) {

            log.error(
                    "Provider database error: paymentId={}",
                    paymentId,
                    ex
            );

            throw new PaymentProviderException(
                    "Provider database failure for paymentId="
                            + paymentId,
                    ex
            );
        }
    }

    private PaymentProviderTransaction findExisting(Long paymentId) {

        try {

            return repository
                    .findByPaymentId(paymentId)
                    .orElse(null);

        } catch (DataAccessException ex) {

            throw new PaymentProviderException(
                    "Unable to check provider transaction for paymentId="
                            + paymentId,
                    ex
            );
        }
    }

    private PaymentProviderResponse toResponse(
            PaymentProviderTransaction transaction) {

        return new PaymentProviderResponse(
                transaction.getProviderOrderId(),
                transaction.getProviderPaymentId(),
                transaction.getProviderReference(),
                transaction.getStatus(),
                null
        );
    }

    private void validateRequest(PaymentProviderRequest request) {

        if (request == null) {

            throw InvalidPaymentProviderRequestException
                    .requestNull();
        }

        if (request.paymentId() == null) {

            throw InvalidPaymentProviderRequestException
                    .paymentIdMissing();
        }

        if (request.orderId() == null) {

            throw InvalidPaymentProviderRequestException
                    .orderIdMissing();
        }

        if (request.amount() == null ||
                request.amount().signum() <= 0) {

            throw InvalidPaymentProviderRequestException
                    .amountInvalid();
        }

        if (request.currency() == null) {

            throw InvalidPaymentProviderRequestException
                    .currencyMissing();
        }

        if (request.paymentMethod() == null) {

            throw InvalidPaymentProviderRequestException
                    .paymentMethodMissing();
        }
    }

    @Override
    public PaymentProviderResponse verifyPayment(String providerReference) {

        PaymentProviderTransaction transaction =
                repository
                        .findByProviderReference(providerReference)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Mock provider transaction not found: "
                                                + providerReference
                                )
                        );

        return new PaymentProviderResponse(
                transaction.getProviderOrderId(),
                transaction.getProviderPaymentId(),
                transaction.getProviderReference(),
                PaymentStatus.SUCCESS,
                null
        );
    }

    @Override
    public PaymentProviderResponse refundPayment(String providerReference,
                                                 BigDecimal amount) {

        PaymentProviderTransaction transaction =
                repository
                        .findByProviderReference(providerReference)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Mock provider transaction not found: "
                                                + providerReference
                                )
                        );

        return new PaymentProviderResponse(
                transaction.getProviderOrderId(),
                transaction.getProviderPaymentId(),
                transaction.getProviderReference(),
                PaymentStatus.REFUNDED,
                null
        );
    }
}