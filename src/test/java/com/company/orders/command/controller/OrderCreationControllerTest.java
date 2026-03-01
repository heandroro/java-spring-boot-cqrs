package com.company.orders.command.controller;

import com.company.orders.command.handler.CreateOrderCommandHandler;
import com.company.orders.command.model.CreateOrderCommand;
import com.company.orders.command.model.CreateOrderResult;
import com.company.orders.shared.model.OrderItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCreationController Tests")
class OrderCreationControllerTest {

    @Mock
    private CreateOrderCommandHandler handler;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderCreationController controller;

    private UUID customerId;
    private CreateOrderCommand command;
    private CreateOrderResult result;
    private OrderItemDto itemDto;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        itemDto = new OrderItemDto("product-123", 2, BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98));
        command = new CreateOrderCommand(customerId, Arrays.asList(itemDto));
        result = new CreateOrderResult(
            UUID.randomUUID(),
            customerId,
            "PENDING",
            BigDecimal.valueOf(199.98),
            OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create order successfully with valid authentication")
    void testCreateOrder_WithValidAuthentication() {
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        when(handler.handle(eq(command), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(command, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(result, response.getBody());
        verify(handler).handle(eq(command), eq(customerId));
    }

    @Test
    @DisplayName("Should extract customer ID from valid UUID principal")
    void testExtractCustomerId_ValidUUID() {
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        when(handler.handle(eq(command), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(command, authentication);

        assertNotNull(response);
        verify(handler).handle(eq(command), eq(customerId));
    }

    @Test
    @DisplayName("Should use fallback customer ID when authentication is null")
    void testExtractCustomerId_NullAuthentication() {
        when(handler.handle(eq(command), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(command, null);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(handler).handle(eq(command), eq(customerId));
    }

    @Test
    @DisplayName("Should use fallback customer ID when principal is null")
    void testExtractCustomerId_NullPrincipal() {
        when(authentication.getPrincipal()).thenReturn(null);
        when(handler.handle(eq(command), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(command, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(handler).handle(eq(command), eq(customerId));
    }

    @Test
    @DisplayName("Should use fallback when principal is not a valid UUID")
    void testExtractCustomerId_InvalidUUID() {
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn("not-a-uuid");
        when(handler.handle(eq(command), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(command, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(handler).handle(eq(command), eq(customerId));
    }

    @Test
    @DisplayName("Should generate random UUID when both authentication and fallback are null")
    void testExtractCustomerId_NullAuthenticationAndNullFallback() {
        CreateOrderCommand commandWithoutCustomer = new CreateOrderCommand(null, Arrays.asList(itemDto));
        when(handler.handle(eq(commandWithoutCustomer), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(commandWithoutCustomer, null);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(handler).handle(eq(commandWithoutCustomer), any(UUID.class));
    }

    @Test
    @DisplayName("Should use fallback customer ID when authentication is null and fallback is provided")
    void testExtractCustomerId_NullAuthenticationWithFallback() {
        UUID fallbackId = UUID.randomUUID();
        CreateOrderCommand commandWithFallback = new CreateOrderCommand(fallbackId, Arrays.asList(itemDto));
        when(handler.handle(eq(commandWithFallback), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(commandWithFallback, null);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(handler).handle(eq(commandWithFallback), eq(fallbackId));
    }

    @Test
    @DisplayName("Should use fallback when UUID parsing fails and fallback is null")
    void testExtractCustomerId_InvalidUUIDWithNullFallback() {
        CreateOrderCommand commandWithoutCustomer = new CreateOrderCommand(null, Arrays.asList(itemDto));
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn("not-a-uuid");
        when(handler.handle(eq(commandWithoutCustomer), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(commandWithoutCustomer, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(handler).handle(eq(commandWithoutCustomer), any(UUID.class));
    }

    @Test
    @DisplayName("Should use fallback when UUID parsing fails and fallback is provided")
    void testExtractCustomerId_InvalidUUIDWithFallback() {
        UUID fallbackId = UUID.randomUUID();
        CreateOrderCommand commandWithFallback = new CreateOrderCommand(fallbackId, Arrays.asList(itemDto));
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn("not-a-uuid");
        when(handler.handle(eq(commandWithFallback), any(UUID.class))).thenReturn(result);

        ResponseEntity<CreateOrderResult> response = controller.createOrder(commandWithFallback, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(handler).handle(eq(commandWithFallback), eq(fallbackId));
    }
}
