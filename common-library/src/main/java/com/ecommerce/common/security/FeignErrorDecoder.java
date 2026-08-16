package com.ecommerce.common.security;

import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.ecommerce.common.exception.RemoteServiceException;
import com.ecommerce.common.exception.RemoteServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        String serviceName =
                response.request()
                        .requestTemplate()
                        .feignTarget()
                        .name();

        int status = response.status();

        if (status == 404) {

            return new RemoteResourceNotFoundException(
                    serviceName,
                    "Resource was not found"
            );
        }

        if (status == 401) {

            return new RemoteServiceException(
                    serviceName,
                    status
            );
        }

        if (status == 403) {

            return new RemoteServiceException(
                    serviceName,
                    status
            );
        }

        if (status >= 500) {

            return new RemoteServiceUnavailableException(
                    serviceName
            );
        }

        return new RemoteServiceException(
                serviceName,
                status
        );
    }
}