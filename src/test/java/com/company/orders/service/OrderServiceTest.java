package com.company.orders.service;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.exception.AuthorizationException;
import com.company.orders.exception.OrderException;
import com.company.orders.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private OrderCreation orderCreation;

    @Mock
    private OrderQuery orderQuery;

    @InjectMocks
    private OrderService service;

    private OrderDto orderDto;
    private CreateOrderRequest createRequest;
    private UUID testCustomerId;
    private UUID testOrderId;

    @BeforeEach
    void setUp() {
        testCustomerId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();

        orderDto = new OrderDto();
        orderDto.setId(testOrderId);
        orderDto.setCustomerId(testCustomerId);
        orderDto.setStatus("pending");
        orderDto.setTotal(BigDecimal.valueOf(249.97));
        orderDto.setCreatedAt(OffsetDateTime.now());

        OrderItemDto itemDto = new OrderItemDto(
            "p123",
            2,
            BigDecimal.valueOf(99.99),
            BigDecimal.valueOf(199.98)
        );

        createRequest = new CreateOrderRequest(
            testCustomerId,
            Arrays.asList(itemDto)
        );
    }

    @Test
    @DisplayName("Should delegate createOrder to OrderCreation")
    void testCreateOrder_ShouldDelegate() {
        when(orderCreation.createOrder(createRequest, testCustomerId)).thenReturn(orderDto);

        OrderDto result = service.createOrder(createRequest, testCustomerId);

        assertNotNull(result);
        assertEquals(testCustomerId, result.getCustomerId());
        verify(orderCreation).createOrder(createRequest, testCustomerId);
    }

    @Test
    @DisplayName("Should propagate AuthorizationException from OrderCreation")
    void testCreateOrder_ShouldPropagateAuthorizationException() {
        UUID differentCustomerId = UUID.randomUUID();
        when(orderCreation.createOrder(createRequest, differentCustomerId))
            .thenThrow(new AuthorizationException("Cannot create order for another customer"));

        assertThrows(AuthorizationException.class, 
            () -> service.createOrder(createRequest, differentCustomerId));
    }

    @Test
    @DisplayName("Should propagate OrderException from OrderCreation")
    void testCreateOrder_ShouldPropagateOrderException() {
        CreateOrderRequest emptyRequest = new CreateOrderRequest(testCustomerId, Arrays.asList());
        when(orderCreation.createOrder(emptyRequest, testCustomerId))
            .thenThrow(new OrderException("Order must have at least one item"));

        assertThrows(OrderException.class, 
            () -> service.createOrder(emptyRequest, testCustomerId));
    }

    @Test
    @DisplayName("Should delegate getOrder to OrderQuery")
    void testGetOrder_ShouldDelegate() {
        when(orderQuery.getOrder(testOrderId, testCustomerId, false)).thenReturn(orderDto);

        OrderDto result = service.getOrder(testOrderId, testCustomerId, false);

        assertNotNull(result);
        assertEquals(testOrderId, result.getId());
        verify(orderQuery).getOrder(testOrderId, testCustomerId, false);
    }

    @Test
    @DisplayName("Should propagate ResourceNotFoundException from OrderQuery")
    void testGetOrder_ShouldPropagateResourceNotFoundException() {
        when(orderQuery.getOrder(testOrderId, testCustomerId, false))
            .thenThrow(new ResourceNotFoundException("Order not found with id: " + testOrderId));

        assertThrows(ResourceNotFoundException.class, 
            () -> service.getOrder(testOrderId, testCustomerId, false));
    }

    @Test
    @DisplayName("Should propagate AuthorizationException from OrderQuery")
    void testGetOrder_ShouldPropagateAuthorizationException() {
        UUID differentCustomerId = UUID.randomUUID();
        when(orderQuery.getOrder(testOrderId, differentCustomerId, false))
            .thenThrow(new AuthorizationException("You do not have access to this order"));

        assertThrows(AuthorizationException.class, 
            () -> service.getOrder(testOrderId, differentCustomerId, false));
    }

    @Test
    @DisplayName("Should delegate listOrders to OrderQuery")
    void testListOrders_ShouldDelegate() {
        OrderListResponse response = new OrderListResponse(Arrays.asList(orderDto), 1L, 20, 0);
        when(orderQuery.listOrders(testCustomerId, false, 20, 0, null)).thenReturn(response);

        OrderListResponse result = service.listOrders(testCustomerId, false, 20, 0, null);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals(1L, result.totalCount());
        verify(orderQuery).listOrders(testCustomerId, false, 20, 0, null);
    }

    @Test
    @DisplayName("Should delegate listOrders with status filter to OrderQuery")
    void testListOrders_WithStatusFilter_ShouldDelegate() {
        OrderListResponse response = new OrderListResponse(Arrays.asList(orderDto), 1L, 20, 0);
        when(orderQuery.listOrders(testCustomerId, false, 20, 0, "pending")).thenReturn(response);

        OrderListResponse result = service.listOrders(testCustomerId, false, 20, 0, "pending");

        assertNotNull(result);
        assertEquals(1, result.data().size());
        verify(orderQuery).listOrders(testCustomerId, false, 20, 0, "pending");
    }

    @Test
    @DisplayName("Should delegate admin listOrders to OrderQuery")
    void testListOrders_AsAdmin_ShouldDelegate() {
        OrderListResponse response = new OrderListResponse(Arrays.asList(orderDto), 1L, 20, 0);
        when(orderQuery.listOrders(testCustomerId, true, 20, 0, null)).thenReturn(response);

        OrderListResponse result = service.listOrders(testCustomerId, true, 20, 0, null);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        verify(orderQuery).listOrders(testCustomerId, true, 20, 0, null);
    }
}
