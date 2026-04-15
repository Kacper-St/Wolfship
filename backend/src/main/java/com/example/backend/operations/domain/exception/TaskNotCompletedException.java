package com.example.backend.operations.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class TaskNotCompletedException extends BaseBusinessException {
    public TaskNotCompletedException(String trackingNumber, String previousTaskType) {
        super("Cannot scan shipment " + trackingNumber + " — previous task not completed: " + previousTaskType, HttpStatus.CONFLICT);
    }
}