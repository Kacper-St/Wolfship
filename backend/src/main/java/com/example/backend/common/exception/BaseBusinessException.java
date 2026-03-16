package com.example.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseBusinessException extends RuntimeException {

    private final HttpStatus httpStatus;

    public BaseBusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
