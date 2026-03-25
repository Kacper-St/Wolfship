package com.example.backend.shipping.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class InvalidAddressException extends BaseBusinessException {

    public InvalidAddressException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}