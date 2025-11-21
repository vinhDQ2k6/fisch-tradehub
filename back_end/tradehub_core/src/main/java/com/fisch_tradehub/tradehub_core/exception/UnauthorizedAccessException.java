package com.fisch_tradehub.tradehub_core.exception;

/**
 * Exception thrown when a user attempts to access a resource without proper authorization.
 */
public class UnauthorizedAccessException extends RuntimeException {
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
