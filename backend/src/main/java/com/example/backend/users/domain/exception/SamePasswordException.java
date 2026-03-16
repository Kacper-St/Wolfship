package com.example.backend.users.domain.exception;

import com.example.backend.common.exception.BaseBusinessException;
import org.springframework.http.HttpStatus;

public class SamePasswordException extends BaseBusinessException {
    public SamePasswordException() {
        super("New password cannot be the same as the current one", HttpStatus.BAD_REQUEST);
    }
}
