package com.ecommerce.common.exception;

public class RemoteServiceException extends RuntimeException {

    private final int status;

    public RemoteServiceException(String serviceName, int status) {

        super(serviceName +
                        " request failed with status " +
                        status
        );

        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}