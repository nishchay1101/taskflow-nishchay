package com.taskflow.common;

import com.taskflow.common.exception.DuplicateEmailException;
import com.taskflow.common.exception.InvalidCredentialsException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.common.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 — Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex,
                                                HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation failed on {} {}: {}", request.getMethod(), request.getRequestURI(), fields);
        return Map.of(
                "error", "validation_failed",
                "fields", fields
        );
    }

    // 401 — Invalid credentials
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(InvalidCredentialsException ex,
                                                        HttpServletRequest request) {
        log.warn("Auth failure on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    // 403 — Forbidden
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleForbidden(ForbiddenException ex,
                                               HttpServletRequest request) {
        log.warn("Forbidden on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    // 404 — Not found
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(ResourceNotFoundException ex,
                                              HttpServletRequest request) {
        log.warn("Not found on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    // 409 — Duplicate email
    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicateEmail(DuplicateEmailException ex,
                                                    HttpServletRequest request) {
        log.warn("Conflict on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                HttpServletRequest request) {
        Class<?> type = ex.getRequiredType();
        String message = (type != null && type.isEnum())
                ? "must be one of: " + Arrays.stream(type.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "))
                : "invalid value: " + ex.getValue();
        log.warn("Type mismatch on {} {}: param={} {}",
                request.getMethod(), request.getRequestURI(), ex.getName(), message);
        return Map.of("error", "validation_failed", "fields", Map.of(ex.getName(), message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnreadable(HttpMessageNotReadableException ex,
                                                HttpServletRequest request) {
        log.warn("Malformed request body on {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return Map.of("error", "malformed request body");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex);  // passes full stack trace to logger, not to client
        return Map.of("error", "internal_server_error");
    }
}