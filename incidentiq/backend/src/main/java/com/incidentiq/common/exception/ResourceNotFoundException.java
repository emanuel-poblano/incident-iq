package com.incidentiq.common.exception;

/** Thrown when a requested Incident, Investigation, or related entity does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
