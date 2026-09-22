package com.ecommerce.common.exception;

import lombok.Getter;

@Getter
public class RemoteResourceNotFoundException extends RuntimeException {

    private final String serviceName;

    private final String remoteMessage;

    public RemoteResourceNotFoundException(String serviceName,
                                           String message) {

        super(serviceName + ": " + message);

        this.serviceName = serviceName;
        this.remoteMessage = message;
    }
}