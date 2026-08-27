package com.example.payment_service.exception;

public class InvalidPaymentProviderRequestException
        extends PaymentProviderException {

    public InvalidPaymentProviderRequestException() {
        super("Invalid payment provider request");
    }

    public InvalidPaymentProviderRequestException(
            String message) {

        super(message);
    }

    public InvalidPaymentProviderRequestException(
            String message,
            Throwable cause) {

        super(message, cause);
    }

    public static InvalidPaymentProviderRequestException requestNull() {
        return new InvalidPaymentProviderRequestException(
                "Payment provider request must not be null"
        );
    }

    public static InvalidPaymentProviderRequestException paymentIdMissing() {
        return new InvalidPaymentProviderRequestException(
                "Payment provider request must contain paymentId"
        );
    }

    public static InvalidPaymentProviderRequestException orderIdMissing() {
        return new InvalidPaymentProviderRequestException(
                "Payment provider request must contain orderId"
        );
    }

    public static InvalidPaymentProviderRequestException amountInvalid() {
        return new InvalidPaymentProviderRequestException(
                "Payment provider amount must be greater than zero"
        );
    }

    public static InvalidPaymentProviderRequestException currencyMissing() {
        return new InvalidPaymentProviderRequestException(
                "Payment provider currency must not be null"
        );
    }

    public static InvalidPaymentProviderRequestException paymentMethodMissing() {
        return new InvalidPaymentProviderRequestException(
                "Payment provider paymentMethod must not be null"
        );
    }
}
