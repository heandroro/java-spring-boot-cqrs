package com.company.orders.command.service;

import com.company.orders.dto.OrderItemDto;
import com.company.orders.shared.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderValidator Tests")
class OrderValidatorTest {

    private OrderValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrderValidator();
    }

    @Test
    @DisplayName("Should validate item successfully when all fields are valid")
    void validateItem_Success() {
        OrderItemDto item = new OrderItemDto("p123", 2, BigDecimal.valueOf(99.99), null);
        
        assertDoesNotThrow(() -> validator.validateItem(item));
    }

    @Test
    @DisplayName("Should throw exception when quantity is null")
    void validateItem_QuantityNull() {
        OrderItemDto item = new OrderItemDto("p123", null, BigDecimal.valueOf(99.99), null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item quantity must be at least 1", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when quantity is less than 1")
    void validateItem_QuantityLessThanOne() {
        OrderItemDto item = new OrderItemDto("p123", 0, BigDecimal.valueOf(99.99), null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item quantity must be at least 1", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when price is null")
    void validateItem_PriceNull() {
        OrderItemDto item = new OrderItemDto("p123", 2, null, null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item price must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when price is zero")
    void validateItem_PriceZero() {
        OrderItemDto item = new OrderItemDto("p123", 2, BigDecimal.ZERO, null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item price must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when price is negative")
    void validateItem_PriceNegative() {
        OrderItemDto item = new OrderItemDto("p123", 2, BigDecimal.valueOf(-10), null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item price must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when productId is null")
    void validateItem_ProductIdNull() {
        OrderItemDto item = new OrderItemDto(null, 2, BigDecimal.valueOf(99.99), null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item product ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when productId is empty")
    void validateItem_ProductIdEmpty() {
        OrderItemDto item = new OrderItemDto("", 2, BigDecimal.valueOf(99.99), null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item product ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when productId is whitespace")
    void validateItem_ProductIdWhitespace() {
        OrderItemDto item = new OrderItemDto("   ", 2, BigDecimal.valueOf(99.99), null);
        
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateItem(item));
        assertEquals("Item product ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate order total successfully when greater than zero")
    void validateOrderTotal_Success() {
        assertDoesNotThrow(() -> validator.validateOrderTotal(BigDecimal.valueOf(100)));
    }

    @Test
    @DisplayName("Should throw exception when order total is zero")
    void validateOrderTotal_Zero() {
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateOrderTotal(BigDecimal.ZERO));
        assertEquals("Order total must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when order total is negative")
    void validateOrderTotal_Negative() {
        OrderException exception = assertThrows(OrderException.class, 
            () -> validator.validateOrderTotal(BigDecimal.valueOf(-10)));
        assertEquals("Order total must be greater than zero", exception.getMessage());
    }
}
