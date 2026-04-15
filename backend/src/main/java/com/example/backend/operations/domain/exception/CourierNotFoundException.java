package com.example.backend.operations.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class CourierNotFoundException extends BaseBusinessException {
    public CourierNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}