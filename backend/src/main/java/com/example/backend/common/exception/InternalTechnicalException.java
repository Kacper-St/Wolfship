package com.example.backend.common.exception;

import org.springframework.http.HttpStatus;

public class InternalTechnicalException extends BaseBusinessException {

    public InternalTechnicalException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
