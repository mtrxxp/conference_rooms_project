package com.mtrxxp.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified error response body. The fieldErrors property is present
 * only for validation errors (omitted otherwise).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse validation(int status, String error, String message,
                                           String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }
}
