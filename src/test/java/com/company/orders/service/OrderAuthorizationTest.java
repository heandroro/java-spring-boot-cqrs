package com.company.orders.service;

import com.company.orders.entity.Order;
import com.company.orders.exception.AuthorizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderAuthorization Tests")
class OrderAuthorizationTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private OrderAuthorization authorization;

    private UUID customerId;
    private UUID differentCustomerId;
    private Order order;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        differentCustomerId = UUID.randomUUID();
        
        order = new Order();
        order.setCustomerId(customerId);
    }

    @Test
    @DisplayName("Should allow create order when customer IDs match")
    void validateCreateOrderAuthorization_Success() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        assertDoesNotThrow(() -> 
            authorization.validateCreateOrderAuthorization(customerId, customerId));
    }

    @Test
    @DisplayName("Should throw exception when creating order for different customer")
    void validateCreateOrderAuthorization_DifferentCustomer() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        AuthorizationException exception = assertThrows(AuthorizationException.class, 
            () -> authorization.validateCreateOrderAuthorization(customerId, differentCustomerId));
        assertEquals("Cannot create order for another customer", exception.getMessage());
    }

    @Test
    @DisplayName("Should allow create order for different customer in test profile")
    void validateCreateOrderAuthorization_TestProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        assertDoesNotThrow(() -> 
            authorization.validateCreateOrderAuthorization(customerId, differentCustomerId));
    }

    @Test
    @DisplayName("Should allow order access when customer IDs match")
    void validateOrderAccess_Success() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        assertDoesNotThrow(() -> 
            authorization.validateOrderAccess(order, customerId, false));
    }

    @Test
    @DisplayName("Should throw exception when accessing different customer's order")
    void validateOrderAccess_DifferentCustomer() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        AuthorizationException exception = assertThrows(AuthorizationException.class, 
            () -> authorization.validateOrderAccess(order, differentCustomerId, false));
        assertEquals("You do not have access to this order", exception.getMessage());
    }

    @Test
    @DisplayName("Should allow admin to access any order")
    void validateOrderAccess_Admin() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        assertDoesNotThrow(() -> 
            authorization.validateOrderAccess(order, differentCustomerId, true));
    }

    @Test
    @DisplayName("Should allow access to different customer's order in test profile")
    void validateOrderAccess_TestProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        assertDoesNotThrow(() -> 
            authorization.validateOrderAccess(order, differentCustomerId, false));
    }
}
