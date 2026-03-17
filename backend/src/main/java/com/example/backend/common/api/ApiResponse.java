package com.example.backend.common.api;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        String message,
        T data,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(message, data, LocalDateTime.now());
    }
}
