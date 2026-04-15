package com.example.backend.operations.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class TaskNotFoundException extends BaseBusinessException {
    public TaskNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}