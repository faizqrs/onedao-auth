package com.onedao.auth.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception ex) {
        return body(HttpStatus.BAD_REQUEST, "Invalid request", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> methodNotAllowed(Exception ex) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> unsupportedMedia(Exception ex) {
        return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type not supported", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(Exception ex) {
        return body(HttpStatus.NOT_FOUND, "Resource not found", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflict(DataIntegrityViolationException ex) {
        return body(HttpStatus.CONFLICT, "Data conflict (duplicate or invalid data)", null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException ex) {
        return body(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception ex) {
        log.error("Unhandled exception", ex);   // real server errors are now visible in the logs
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus s, String msg, Object details) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", s.value());
        m.put("message", msg);
        if (details != null) m.put("errors", details);
        return ResponseEntity.status(s).body(m);
    }
}