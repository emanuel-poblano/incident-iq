package com.incidentiq.common.exception;

/** Thrown when a request is well-formed but cannot be processed (e.g. no logs to investigate). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
