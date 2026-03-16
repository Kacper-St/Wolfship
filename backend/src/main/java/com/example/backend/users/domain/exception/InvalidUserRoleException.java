package com.example.backend.users.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class InvalidUserRoleException extends BaseBusinessException {
    public InvalidUserRoleException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
