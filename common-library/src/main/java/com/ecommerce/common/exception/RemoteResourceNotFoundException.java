package com.ecommerce.common.exception;

public class RemoteResourceNotFoundException extends RuntimeException {

    public RemoteResourceNotFoundException(String serviceName,
                                           String message) {

        super(serviceName + ": " + message);
    }
}