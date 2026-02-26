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
}
