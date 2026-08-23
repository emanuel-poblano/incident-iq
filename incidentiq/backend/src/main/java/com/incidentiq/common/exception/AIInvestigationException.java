package com.incidentiq.common.exception;

/**
 * Thrown when the AI investigation provider is unavailable, times out, or
 * returns a response that fails structural/semantic validation. Callers
 * should never blindly trust or persist an AI response that raised this.
 */
public class AIInvestigationException extends RuntimeException {
    public AIInvestigationException(String message) {
        super(message);
    }

    public AIInvestigationException(String message, Throwable cause) {
        super(message, cause);
    }
}
