package com.company.orders.query.handler;

import com.company.orders.command.service.OrderAuthorization;
import com.company.orders.domain.entity.Order;
import com.company.orders.domain.entity.OrderItem;
import com.company.orders.domain.enums.OrderStatus;
import com.company.orders.query.model.GetOrderQuery;
import com.company.orders.query.model.OrderQueryResult;
import com.company.orders.query.repository.OrderQueryRepository;
import com.company.orders.shared.exception.AuthorizationException;
import com.company.orders.shared.exception.ResourceNotFoundException;
import com.company.orders.shared.mapper.OrderQueryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrderQueryHandler Tests")
class GetOrderQueryHandlerTest {

    @Mock
    private OrderQueryRepository repository;

    @Mock
    private OrderQueryMapper mapper;

    @Mock
    private OrderAuthorization authorization;

    @InjectMocks
    private GetOrderQueryHandler handler;

    private UUID orderId;
    private UUID customerId;
    private Order order;
    private OrderQueryResult expectedResult;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(BigDecimal.valueOf(199.98));
        order.setItems(new ArrayList<>());

        OrderItem item = new OrderItem();
        item.setProductId("product-123");
        item.setQuantity(2);
        item.setPricePerUnit(BigDecimal.valueOf(99.99));
        item.setSubtotal(BigDecimal.valueOf(199.98));
        order.getItems().add(item);

        expectedResult = new OrderQueryResult(
            orderId,
            customerId,
            "pending",
            BigDecimal.valueOf(199.98),
            new ArrayList<>(),
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    @Test
    @DisplayName("Should get order successfully when authorized")
    void testGetOrder_Success() {
        GetOrderQuery query = new GetOrderQuery(orderId, customerId, false);
        
        when(repository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResult(order)).thenReturn(expectedResult);

        OrderQueryResult result = handler.handle(query);

        assertNotNull(result);
        assertEquals(orderId, result.id());
        assertEquals(customerId, result.customerId());

        verify(repository).findByIdWithItems(orderId);
        verify(authorization).validateOrderAccess(order, customerId, false);
        verify(mapper).toResult(order);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void testGetOrder_NotFound() {
        GetOrderQuery query = new GetOrderQuery(orderId, customerId, false);
        
        when(repository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> handler.handle(query));

        verify(repository).findByIdWithItems(orderId);
        verify(authorization, never()).validateOrderAccess(any(), any(), anyBoolean());
        verify(mapper, never()).toResult(any());
    }

    @Test
    @DisplayName("Should throw exception when not authorized")
    void testGetOrder_NotAuthorized() {
        GetOrderQuery query = new GetOrderQuery(orderId, customerId, false);
        
        when(repository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        doThrow(new AuthorizationException("Not authorized"))
                .when(authorization).validateOrderAccess(order, customerId, false);

        assertThrows(AuthorizationException.class, () -> handler.handle(query));

        verify(repository).findByIdWithItems(orderId);
        verify(authorization).validateOrderAccess(order, customerId, false);
        verify(mapper, never()).toResult(any());
    }

    @Test
    @DisplayName("Should allow admin to access any order")
    void testGetOrder_AdminAccess() {
        GetOrderQuery query = new GetOrderQuery(orderId, UUID.randomUUID(), true);
        
        when(repository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(mapper.toResult(order)).thenReturn(expectedResult);

        OrderQueryResult result = handler.handle(query);

        assertNotNull(result);
        assertEquals(orderId, result.id());

        verify(repository).findByIdWithItems(orderId);
        verify(authorization).validateOrderAccess(eq(order), any(UUID.class), eq(true));
        verify(mapper).toResult(order);
    }
}
