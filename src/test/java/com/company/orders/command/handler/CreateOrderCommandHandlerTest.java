package com.company.orders.command.handler;

import com.company.orders.command.model.CreateOrderCommand;
import com.company.orders.command.model.CreateOrderResult;
import com.company.orders.command.repository.OrderCommandRepository;
import com.company.orders.command.service.OrderAuthorization;
import com.company.orders.command.service.OrderValidator;
import com.company.orders.domain.entity.Order;
import com.company.orders.domain.entity.OrderItem;
import com.company.orders.domain.enums.OrderStatus;
import com.company.orders.shared.exception.OrderException;
import com.company.orders.shared.model.OrderItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderCommandHandler Tests")
class CreateOrderCommandHandlerTest {

    @Mock
    private OrderCommandRepository repository;

    @Mock
    private OrderValidator validator;

    @Mock
    private OrderAuthorization authorization;

    @Mock
    private com.company.orders.shared.mapper.OrderCommandMapper mapper;

    @InjectMocks
    private CreateOrderCommandHandler handler;

    private CreateOrderCommand validCommand;
    private UUID customerId;
    private UUID authenticatedCustomerId;
    private OrderItemDto itemDto;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        authenticatedCustomerId = customerId;
        itemDto = new OrderItemDto("product-123", 2, BigDecimal.valueOf(99.99), BigDecimal.valueOf(199.98));
        validCommand = new CreateOrderCommand(customerId, Arrays.asList(itemDto));
    }

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrder_Success() {
        Order order = new Order();
        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setCustomerId(customerId);
        savedOrder.setStatus(OrderStatus.PENDING);
        savedOrder.setTotal(BigDecimal.valueOf(199.98));

        OrderItem item = new OrderItem();
        CreateOrderResult expectedResult = new CreateOrderResult(
            savedOrder.getId(), customerId, "PENDING", BigDecimal.valueOf(199.98), OffsetDateTime.now()
        );

        when(mapper.toEntity(validCommand)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(item);
        when(repository.save(any(Order.class))).thenReturn(savedOrder);
        when(mapper.toResult(savedOrder)).thenReturn(expectedResult);

        CreateOrderResult result = handler.handle(validCommand, authenticatedCustomerId);

        assertNotNull(result);
        assertEquals(savedOrder.getId(), result.orderId());
        assertEquals(customerId, result.customerId());
        assertEquals("PENDING", result.status());

        verify(authorization).validateCreateOrderAuthorization(customerId, authenticatedCustomerId);
        verify(validator).validateItem(any(OrderItemDto.class));
        verify(validator).validateOrderTotal(any(BigDecimal.class));
        verify(repository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when validation fails")
    void testCreateOrder_ValidationFails() {
        Order order = new Order();
        
        when(mapper.toEntity(validCommand)).thenReturn(order);
        doThrow(new OrderException("Invalid item"))
                .when(validator).validateItem(any(OrderItemDto.class));

        assertThrows(OrderException.class, () -> handler.handle(validCommand, authenticatedCustomerId));

        verify(validator).validateItem(any(OrderItemDto.class));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when authorization fails")
    void testCreateOrder_AuthorizationFails() {
        doThrow(new OrderException("Unauthorized"))
                .when(authorization).validateCreateOrderAuthorization(customerId, authenticatedCustomerId);

        assertThrows(OrderException.class, () -> handler.handle(validCommand, authenticatedCustomerId));

        verify(authorization).validateCreateOrderAuthorization(customerId, authenticatedCustomerId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when command has no items")
    void testCreateOrder_NoItems() {
        CreateOrderCommand emptyCommand = new CreateOrderCommand(customerId, Arrays.asList());

        assertThrows(OrderException.class, () -> handler.handle(emptyCommand, authenticatedCustomerId));

        verify(authorization).validateCreateOrderAuthorization(customerId, authenticatedCustomerId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate order total")
    void testCreateOrder_ValidatesTotal() {
        Order order = new Order();
        Order savedOrder = new Order();
        savedOrder.setId(UUID.randomUUID());
        OrderItem item = new OrderItem();
        CreateOrderResult expectedResult = new CreateOrderResult(
            savedOrder.getId(), customerId, "PENDING", BigDecimal.valueOf(199.98), OffsetDateTime.now()
        );

        when(mapper.toEntity(validCommand)).thenReturn(order);
        when(mapper.toItemEntity(any(OrderItemDto.class))).thenReturn(item);
        when(repository.save(any(Order.class))).thenReturn(savedOrder);
        when(mapper.toResult(savedOrder)).thenReturn(expectedResult);

        handler.handle(validCommand, authenticatedCustomerId);

        verify(validator).validateOrderTotal(any(BigDecimal.class));
    }
}
