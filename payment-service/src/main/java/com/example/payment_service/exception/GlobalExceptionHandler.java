package com.example.payment_service.exception;

import com.ecommerce.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                "Payment Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }


    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                "Order Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayment(
            DuplicatePaymentException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                "Duplicate Payment",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(PaymentAlreadyCancelledException.class)
    public ResponseEntity<ErrorResponse> handlePaymentAlreadyCancelled(
            PaymentAlreadyCancelledException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                "Payment Already Cancelled",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(PaymentAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentAlreadyCompleted(
            PaymentAlreadyCompletedException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                "Payment Already Completed",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(PaymentNotPayableException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotPayable(
            PaymentNotPayableException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                "Payment Not Payable",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(PaymentOrderAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentOrderAccessDenied(
            PaymentOrderAccessDeniedException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError ->
                        fieldError.getField()
                                + ": "
                                + fieldError.getDefaultMessage())
                .toList();

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Request validation failed",
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You are not authorized to access this resource.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Access denied.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(InvalidPaymentStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStatusTransition(
            InvalidPaymentStatusTransitionException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                "Invalid Payment Status Transition",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String error,
            String message,
            String path,
            List<String> details) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                path,
                details
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}