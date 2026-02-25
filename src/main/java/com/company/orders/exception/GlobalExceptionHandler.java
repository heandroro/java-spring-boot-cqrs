package com.company.orders.exception;

import com.company.orders.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Resource not found [traceId={}]: {}", traceId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ErrorResponse.ErrorCode.NOT_FOUND,
            ex.getMessage(),
            request.getRequestURI(),
            traceId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(
            AuthorizationException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Authorization error [traceId={}]: {}", traceId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ErrorResponse.ErrorCode.AUTHORIZATION_ERROR,
            ex.getMessage(),
            request.getRequestURI(),
            traceId
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponse> handleOrderException(
            OrderException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Order exception [traceId={}]: {}", traceId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ErrorResponse.ErrorCode.VALIDATION_ERROR,
            ex.getMessage(),
            request.getRequestURI(),
            traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Validation error [traceId={}]: {}", traceId, ex.getMessage());
        
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
            ErrorResponse.ErrorCode.VALIDATION_ERROR,
            "Invalid input data",
            request.getRequestURI(),
            traceId
        );
        error.setDetails(details);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Illegal argument [traceId={}]: {}", traceId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ErrorResponse.ErrorCode.VALIDATION_ERROR,
            ex.getMessage(),
            request.getRequestURI(),
            traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unexpected error [traceId={}]", traceId, ex);
        
        ErrorResponse error = new ErrorResponse(
            ErrorResponse.ErrorCode.INTERNAL_ERROR,
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI(),
            traceId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
