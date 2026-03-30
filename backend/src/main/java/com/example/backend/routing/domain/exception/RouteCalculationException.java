package com.example.backend.routing.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class RouteCalculationException extends BaseBusinessException {
    public RouteCalculationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}