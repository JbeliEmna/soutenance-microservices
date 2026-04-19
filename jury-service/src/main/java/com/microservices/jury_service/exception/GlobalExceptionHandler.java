package com.microservices.jury_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des erreurs 404 - Jury non trouvé
    @ExceptionHandler(JuryNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleJuryNotFoundException(
            JuryNotFoundException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .errorCode("JURY_NOT_FOUND")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Gestion des erreurs 409 - Conflit d'affectation
    @ExceptionHandler(ConflitAffectationException.class)
    public ResponseEntity<ErrorDetails> handleConflitAffectationException(
            ConflitAffectationException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .errorCode("CONFLIT_AFFECTATION")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    // Gestion des erreurs de validation (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Gestion des erreurs 400 - Argument invalide
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Gestion des erreurs génériques (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message("Une erreur interne est survenue: " + ex.getMessage())
                .errorCode("INTERNAL_SERVER_ERROR")
                .path(request.getRequestURI())
                .details(ex.getClass().getSimpleName())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Gestion des erreurs d'état illégal (conflit)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetails> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .errorCode("ILLEGAL_STATE")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}