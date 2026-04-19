package com.microservices.jury_service.exception;

public class JuryNotFoundException extends RuntimeException {

    public JuryNotFoundException(String message) {
        super(message);
    }

    public JuryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}