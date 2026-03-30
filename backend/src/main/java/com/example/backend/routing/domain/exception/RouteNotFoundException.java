package com.example.backend.routing.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class RouteNotFoundException extends BaseBusinessException {
    public RouteNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}