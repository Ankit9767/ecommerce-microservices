package com.ecommerce.common.security;

import com.ecommerce.common.dto.ErrorResponse;
import com.ecommerce.common.exception.RemoteResourceNotFoundException;
import com.ecommerce.common.exception.RemoteServiceException;
import com.ecommerce.common.exception.RemoteServiceUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {

        String serviceName =
                response.request()
                        .requestTemplate()
                        .feignTarget()
                        .name();

        int status = response.status();

        ErrorResponse remoteError =
                readErrorResponse(response);

        String remoteMessage =
                remoteError != null
                        ? remoteError.message()
                        : null;

        String remoteErrorType =
                remoteError != null
                        ? remoteError.error()
                        : null;

        if (status == 404) {

            return new RemoteResourceNotFoundException(
                    serviceName,
                    remoteMessage != null
                            ? remoteMessage
                            : "Resource was not found"
            );
        }

        if (status == 401 || status == 403) {

            return new RemoteServiceException(
                    serviceName,
                    status,
                    remoteErrorType,
                    remoteMessage
            );
        }

        if (status >= 500) {

            return new RemoteServiceUnavailableException(
                    serviceName,
                    remoteMessage
            );
        }

        return new RemoteServiceException(
                serviceName,
                status,
                remoteErrorType,
                remoteMessage
        );
    }

    private ErrorResponse readErrorResponse(Response response) {

        if (response.body() == null) {
            return null;
        }

        try (InputStream inputStream =
                     response.body().asInputStream()) {

            return objectMapper.readValue(
                    inputStream,
                    ErrorResponse.class
            );

        } catch (IOException ex) {

            return null;
        }
    }
}