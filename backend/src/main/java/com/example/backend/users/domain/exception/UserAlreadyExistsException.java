package com.example.backend.users.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BaseBusinessException {

    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}