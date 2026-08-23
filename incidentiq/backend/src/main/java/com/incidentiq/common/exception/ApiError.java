package com.incidentiq.common.exception;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error shape returned to the frontend for every failure case.
 * Never includes stack traces — technical details are logged server-side
 * with the correlating requestId instead.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, message, path, details);
    }
}
