package com.rsl.leavemanagement.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure returned by the global exception handler.
 */
public record ErrorResponseDto(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
    /** Constructor for general errors (no field-level validation map). */
    public ErrorResponseDto(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }
}
