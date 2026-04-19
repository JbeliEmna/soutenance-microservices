package com.microservices.jury_service.exception;

public class ConflitAffectationException extends RuntimeException {

    public ConflitAffectationException(String message) {
        super(message);
    }

    public ConflitAffectationException(String message, Throwable cause) {
        super(message, cause);
    }
}