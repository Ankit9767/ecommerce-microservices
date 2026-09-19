package com.example.payment_service.service.provider;

import com.example.payment_service.config.RazorpayProperties;
import com.example.payment_service.dto.provider.PaymentProviderRequest;
import com.example.payment_service.service.PaymentProvider;
import com.ecommerce.common.dto.PaymentProviderResponse;
import com.ecommerce.common.enums.PaymentStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@ConditionalOnProperty(
        name = "payment.provider",
        havingValue = "RAZORPAY"
)
@RequiredArgsConstructor
@Slf4j
public class RazorpayPaymentProvider implements PaymentProvider {

    private final RazorpayClient razorpayClient;

    private final RazorpayProperties razorpayProperties;

    @Override
    public String getProviderName() {
        return "RAZORPAY";
    }

    @Override
    public PaymentProviderResponse createProviderPaymentTransaction(
            PaymentProviderRequest request) {

        validateRequest(request);

        try {
            long amountInSubunits = convertToSubunits(request.amount());

            JSONObject orderRequest = new JSONObject();

            orderRequest.put(
                    "amount",
                    amountInSubunits
            );

            orderRequest.put(
                    "currency",
                    request.currency().name()
            );

            orderRequest.put(
                    "receipt",
                    buildReceipt(request)
            );

            JSONObject notes = new JSONObject();

            notes.put(
                    "payment_id",
                    String.valueOf(request.paymentId())
            );

            notes.put(
                    "order_id",
                    String.valueOf(request.orderId())
            );

            orderRequest.put(
                    "notes",
                    notes
            );

            log.info(
                    "Creating Razorpay order for paymentId={}, orderId={}, amount={}, currency={}",
                    request.paymentId(),
                    request.orderId(),
                    request.amount(),
                    request.currency()
            );

            Order razorpayOrder =
                    razorpayClient.orders.create(orderRequest);

            String providerOrderId =
                    extractString(razorpayOrder, "id");

            String providerStatus =
                    extractString(razorpayOrder, "status");

            if (providerOrderId == null || providerOrderId.isBlank()) {
                throw new IllegalStateException(
                        "Razorpay returned an order without an order ID"
                );
            }

            log.info(
                    "Razorpay order created successfully. paymentId={}, orderId={}, providerOrderId={}, providerStatus={}",
                    request.paymentId(),
                    request.orderId(),
                    providerOrderId,
                    providerStatus
            );

            return new PaymentProviderResponse(
                    providerOrderId,
                    null,
                    providerOrderId,
                    PaymentStatus.PENDING,
                    null
            );

        } catch (RazorpayException e) {

            log.error(
                    "Razorpay order creation failed. paymentId={}, orderId={}, amount={}, currency={}",
                    request.paymentId(),
                    request.orderId(),
                    request.amount(),
                    request.currency(),
                    e
            );

            /*
             * Do not convert a provider failure into a fake
             * PaymentProviderResponse with SUCCESS/FAILED.
             *
             * The local payment remains PENDING and the caller
             * can handle the provider exception appropriately.
             */
            throw new IllegalStateException(
                    "Unable to create Razorpay payment order: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public PaymentProviderResponse verifyPayment(String providerReference) {

        if (providerReference == null
                || providerReference.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay payment reference cannot be null or blank"
            );
        }

        try {
            com.razorpay.Payment razorpayPayment =
                    razorpayClient.payments.fetch(providerReference);

            String providerPaymentId =
                    extractString(razorpayPayment, "id");

            String providerOrderId =
                    extractString(razorpayPayment, "order_id");

            String providerStatus =
                    extractString(razorpayPayment, "status");

            String failureReason =
                    extractString(
                            razorpayPayment,
                            "error_description"
                    );

            PaymentStatus paymentStatus =
                    mapRazorpayPaymentStatus(providerStatus);

            log.info(
                    "Razorpay payment verified. providerPaymentId={}, providerOrderId={}, providerStatus={}, localStatus={}",
                    providerPaymentId,
                    providerOrderId,
                    providerStatus,
                    paymentStatus
            );

            return new PaymentProviderResponse(
                    providerOrderId,
                    providerPaymentId,
                    providerPaymentId,
                    paymentStatus,
                    failureReason
            );

        } catch (RazorpayException e) {

            log.error(
                    "Failed to verify Razorpay payment. providerReference={}",
                    providerReference,
                    e
            );

            throw new IllegalStateException(
                    "Unable to verify Razorpay payment: "
                            + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public PaymentProviderResponse refundPayment(String providerReference,
                                                 BigDecimal amount) {

        if (providerReference == null
                || providerReference.isBlank()) {

            throw new IllegalArgumentException(
                    "Razorpay payment reference cannot be null or blank"
            );
        }

        if (amount == null
                || amount.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Refund amount must be greater than zero"
            );
        }

        try {

            JSONObject refundRequest = new JSONObject();

            refundRequest.put(
                    "amount",
                    convertToSubunits(amount)
            );

            log.info(
                    "Creating Razorpay refund. providerPaymentId={}, amount={}",
                    providerReference,
                    amount
            );

            com.razorpay.Refund refund =
                    razorpayClient.payments.refund(
                            providerReference,
                            refundRequest
                    );

            String refundId =
                    extractString(refund, "id");

            log.info(
                    "Razorpay refund created. providerPaymentId={}, refundId={}",
                    providerReference,
                    refundId
            );

            return new PaymentProviderResponse(
                    null,
                    providerReference,
                    refundId,
                    PaymentStatus.REFUNDED,
                    null
            );

        } catch (RazorpayException e) {

            log.error(
                    "Razorpay refund failed. providerPaymentId={}, amount={}",
                    providerReference,
                    amount,
                    e
            );

            throw new IllegalStateException(
                    "Unable to refund Razorpay payment: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private void validateRequest(PaymentProviderRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Payment provider request cannot be null"
            );
        }

        if (request.paymentId() == null) {
            throw new IllegalArgumentException(
                    "Payment ID cannot be null"
            );
        }

        if (request.orderId() == null) {
            throw new IllegalArgumentException(
                    "Order ID cannot be null"
            );
        }

        if (request.amount() == null
                || request.amount().signum() <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (request.currency() == null) {
            throw new IllegalArgumentException(
                    "Payment currency cannot be null"
            );
        }
    }

    private long convertToSubunits(BigDecimal amount) {

        return amount
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact();
    }

    private String buildReceipt(PaymentProviderRequest request) {

        return "payment_" + request.paymentId()
                + "_order_" + request.orderId();
    }

    private String extractString(Object resource, String field) {

        if (resource == null) {
            return null;
        }

        try {
            Object value;

            if (resource instanceof Order order) {
                value = order.get(field);

            } else if (resource instanceof com.razorpay.Payment payment) {
                value = payment.get(field);

            } else if (resource instanceof com.razorpay.Refund refund) {
                value = refund.get(field);

            } else {
                return null;
            }

            return value != null
                    ? value.toString()
                    : null;

        } catch (Exception e) {

            log.warn(
                    "Unable to extract Razorpay field '{}'",
                    field,
                    e
            );

            return null;
        }
    }

    private PaymentStatus mapRazorpayPaymentStatus(String razorpayStatus) {

        if (razorpayStatus == null) {
            return PaymentStatus.PENDING;
        }

        return switch (razorpayStatus.toLowerCase()) {

            case "captured" ->
                    PaymentStatus.SUCCESS;

            case "failed" ->
                    PaymentStatus.FAILED;

            case "refunded" ->
                    PaymentStatus.REFUNDED;

            case "authorized" ->
                    PaymentStatus.PROCESSING;

            case "created" ->
                    PaymentStatus.PENDING;

            default -> {
                log.warn(
                        "Unknown Razorpay payment status: {}",
                        razorpayStatus
                );

                yield PaymentStatus.PENDING;
            }
        };
    }
}