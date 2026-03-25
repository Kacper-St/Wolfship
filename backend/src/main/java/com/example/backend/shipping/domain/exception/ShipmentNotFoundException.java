package com.example.backend.shipping.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class ShipmentNotFoundException extends BaseBusinessException {

    public ShipmentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}