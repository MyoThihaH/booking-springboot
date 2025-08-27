package com.myo.booking.exception;

import com.myo.booking.dto.response.StandardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<?>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String message = "Validation failed: " + errors.toString();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.error(message));
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.error("Invalid email or password"));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardResponse<?>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.error("Authentication failed"));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(StandardResponse.error("Access denied"));
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardResponse<?>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(StandardResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StandardResponse<?>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception occurred", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<?>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = status.value() >= 500 ? "An internal server error occurred" : ex.getMessage();
        
        return ResponseEntity.status(status)
                .body(StandardResponse.error(message));
    }
}