package com.example.backend.shipping.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class LabelGenerationException extends BaseBusinessException {

    public LabelGenerationException(String trackingNumber) {
        super("Failed to generate label for shipment: " + trackingNumber,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}