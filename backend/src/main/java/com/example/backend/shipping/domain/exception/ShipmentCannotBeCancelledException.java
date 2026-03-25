package com.example.backend.shipping.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class ShipmentCannotBeCancelledException extends BaseBusinessException {

    public ShipmentCannotBeCancelledException(String trackingNumber, String status) {
        super("Shipment " + trackingNumber + " cannot be cancelled with status: " + status,
                HttpStatus.CONFLICT);
    }
}