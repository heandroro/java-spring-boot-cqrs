package com.company.orders.shared.exception;

import com.company.orders.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test-uri");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void handleValidationExceptions() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // When
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleValidationException(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorResponse.ErrorCode.VALIDATION_ERROR, responseEntity.getBody().code());
        assertTrue(responseEntity.getBody().details().contains("fieldName: defaultMessage"));
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void handleResourceNotFoundException() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        // When
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleResourceNotFound(ex, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorResponse.ErrorCode.NOT_FOUND, responseEntity.getBody().code());
        assertEquals("Resource not found", responseEntity.getBody().message());
    }

    @Test
    @DisplayName("Should handle AuthorizationException")
    void handleAuthorizationException() {
        // Given
        AuthorizationException ex = new AuthorizationException("Access denied");

        // When
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleAuthorizationException(ex, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorResponse.ErrorCode.AUTHORIZATION_ERROR, responseEntity.getBody().code());
        assertEquals("Access denied", responseEntity.getBody().message());
    }

    @Test
    @DisplayName("Should handle OrderException")
    void handleOrderException() {
        // Given
        OrderException ex = new OrderException("Invalid order");

        // When
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleOrderException(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorResponse.ErrorCode.VALIDATION_ERROR, responseEntity.getBody().code());
        assertEquals("Invalid order", responseEntity.getBody().message());
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void handleGlobalException() {
        // Given
        Exception ex = new Exception("Generic error");

        // When
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleGenericException(ex, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorResponse.ErrorCode.INTERNAL_ERROR, responseEntity.getBody().code());
        assertEquals("An unexpected error occurred. Please try again later.", responseEntity.getBody().message());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void handleIllegalArgumentException() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleIllegalArgument(ex, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(ErrorResponse.ErrorCode.VALIDATION_ERROR, responseEntity.getBody().code());
        assertEquals("Invalid argument", responseEntity.getBody().message());
    }
}
