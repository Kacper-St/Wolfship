package com.example.backend.operations.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class UnauthorizedScanException extends BaseBusinessException {
    public UnauthorizedScanException(String trackingNumber) {
        super("Courier is not authorized to scan shipment: " + trackingNumber, HttpStatus.FORBIDDEN);
    }
}