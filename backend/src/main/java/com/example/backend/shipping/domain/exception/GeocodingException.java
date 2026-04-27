package com.example.backend.shipping.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class GeocodingException extends BaseBusinessException {

    public GeocodingException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}