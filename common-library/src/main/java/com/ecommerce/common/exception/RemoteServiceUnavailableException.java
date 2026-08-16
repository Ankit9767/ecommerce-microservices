package com.ecommerce.common.exception;

public class RemoteServiceUnavailableException extends RuntimeException {

    public RemoteServiceUnavailableException(String serviceName) {

        super(serviceName + " is currently unavailable");
    }

    public RemoteServiceUnavailableException(String serviceName,
                                             Throwable cause) {

        super(serviceName + " is currently unavailable",
                cause
        );
    }
}