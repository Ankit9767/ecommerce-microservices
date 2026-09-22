package com.ecommerce.common.exception;

import lombok.Getter;

@Getter
public class RemoteServiceException extends RuntimeException {

    private final String serviceName;

    private final int status;

    private final String remoteError;

    private final String remoteMessage;

    public RemoteServiceException(String serviceName,
                                  int status) {

        this(
                serviceName,
                status,
                null,
                null
        );
    }

    public RemoteServiceException(String serviceName,
                                  int status, String remoteError,
                                  String remoteMessage) {

        super(
                remoteMessage != null && !remoteMessage.isBlank()
                        ? remoteMessage
                        : serviceName +
                        " request failed with status " +
                        status
        );

        this.serviceName = serviceName;
        this.status = status;
        this.remoteError = remoteError;
        this.remoteMessage = remoteMessage;
    }
}