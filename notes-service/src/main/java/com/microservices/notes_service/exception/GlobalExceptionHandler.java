package com.microservices.notes_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(buildPayload(exception.getStatus(), exception.getMessage(), request.getRequestURI(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildPayload(HttpStatus.BAD_REQUEST, "Validation error", request.getRequestURI(), errors));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildPayload(
                        HttpStatus.BAD_REQUEST,
                        "JSON parse error: " + exception.getMessage(),
                        request.getRequestURI(),
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildPayload(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        exception.getMessage(),
                        request.getRequestURI(),
                        null
                ));
    }

    private static String formatFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() == null ? "invalid value" : fieldError.getDefaultMessage();
        return fieldError.getField() + ": " + message;
    }

    private static Map<String, Object> buildPayload(
            HttpStatus status,
            String message,
            String path,
            List<String> details
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", LocalDateTime.now());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", message);
        payload.put("path", path);
        if (details != null && !details.isEmpty()) {
            payload.put("details", details);
        }
        return payload;
    }
}
