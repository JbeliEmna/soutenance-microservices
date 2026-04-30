package com.microservices.jury_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String errorCode;
    private String path;
    private String details;

    public ErrorDetails(LocalDateTime timestamp, String message, String errorCode) {
        this.timestamp = timestamp;
        this.message = message;
        this.errorCode = errorCode;
    }

    public ErrorDetails(LocalDateTime timestamp, String message, String errorCode, String path) {
        this.timestamp = timestamp;
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
    }
}