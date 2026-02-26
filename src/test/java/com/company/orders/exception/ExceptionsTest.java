package com.company.orders.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Custom Exceptions Tests")
class ExceptionsTest {

    @Test
    @DisplayName("Should create AuthorizationException with message")
    void createAuthorizationException() {
        String message = "Access denied";
        AuthorizationException exception = new AuthorizationException(message);
        
        assertEquals(message, exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should create ResourceNotFoundException with message")
    void createResourceNotFoundException() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should create OrderException with message")
    void createOrderException() {
        String message = "Invalid order";
        OrderException exception = new OrderException(message);
        
        assertEquals(message, exception.getMessage());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("AuthorizationException should be throwable")
    void authorizationExceptionThrowable() {
        assertThrows(AuthorizationException.class, () -> {
            throw new AuthorizationException("Test");
        });
    }

    @Test
    @DisplayName("ResourceNotFoundException should be throwable")
    void resourceNotFoundExceptionThrowable() {
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Test");
        });
    }

    @Test
    @DisplayName("OrderException should be throwable")
    void orderExceptionThrowable() {
        assertThrows(OrderException.class, () -> {
            throw new OrderException("Test");
        });
    }

    @Test
    @DisplayName("Should create AuthorizationException with message and cause")
    void createAuthorizationExceptionWithCause() {
        String message = "Access denied";
        Throwable cause = new RuntimeException("Root cause");
        AuthorizationException exception = new AuthorizationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should create ResourceNotFoundException with message and cause")
    void createResourceNotFoundExceptionWithCause() {
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Root cause");
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should create OrderException with message and cause")
    void createOrderExceptionWithCause() {
        String message = "Invalid order";
        Throwable cause = new RuntimeException("Root cause");
        OrderException exception = new OrderException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception);
    }
}
