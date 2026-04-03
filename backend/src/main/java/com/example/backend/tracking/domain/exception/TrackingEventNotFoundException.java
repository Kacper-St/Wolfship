package com.example.backend.tracking.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class TrackingEventNotFoundException extends BaseBusinessException {
    public TrackingEventNotFoundException(String trackingNumber) {
        super("No tracking events found for: " + trackingNumber, HttpStatus.NOT_FOUND);
    }
}