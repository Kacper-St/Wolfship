package com.example.backend.users.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseBusinessException {

    public InvalidCredentialsException() {
        super("Invalid password or email", HttpStatus.UNAUTHORIZED);
    }
}

