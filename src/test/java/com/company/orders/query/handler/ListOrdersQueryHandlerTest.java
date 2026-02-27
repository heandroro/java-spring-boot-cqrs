package com.company.orders.query.handler;

import com.company.orders.domain.entity.Order;
import com.company.orders.domain.enums.OrderStatus;
import com.company.orders.query.model.ListOrdersQuery;
import com.company.orders.query.model.OrderListQueryResult;
import com.company.orders.query.model.OrderQueryResult;
import com.company.orders.query.repository.OrderQueryRepository;
import com.company.orders.shared.mapper.OrderQueryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListOrdersQueryHandler Tests")
class ListOrdersQueryHandlerTest {

    @Mock
    private OrderQueryRepository repository;

    @Mock
    private OrderQueryMapper mapper;

    @InjectMocks
    private ListOrdersQueryHandler handler;

    private UUID customerId;
    private List<Order> orders;
    private List<OrderQueryResult> queryResults;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        orders = new ArrayList<>();

        Order order1 = new Order();
        order1.setId(UUID.randomUUID());
        order1.setCustomerId(customerId);
        order1.setStatus(OrderStatus.PENDING);
        order1.setTotal(BigDecimal.valueOf(100.00));
        order1.setItems(new ArrayList<>());

        Order order2 = new Order();
        order2.setId(UUID.randomUUID());
        order2.setCustomerId(customerId);
        order2.setStatus(OrderStatus.CONFIRMED);
        order2.setTotal(BigDecimal.valueOf(200.00));
        order2.setItems(new ArrayList<>());

        orders.add(order1);
        orders.add(order2);

        queryResults = new ArrayList<>();
    }

    @Test
    @DisplayName("Should list orders for customer without status filter")
    void testListOrders_CustomerNoStatus() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, false, 20, 0, null);
        Page<Order> page = new PageImpl<>(orders);

        when(repository.findByCustomerId(eq(customerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(customerId)).thenReturn(2L);
        when(mapper.toResultList(orders)).thenReturn(queryResults);

        OrderListQueryResult result = handler.handle(query);

        assertNotNull(result);
        assertEquals(2L, result.totalCount());
        assertEquals(20, result.limit());
        assertEquals(0, result.offset());

        verify(repository).findByCustomerId(eq(customerId), any(Pageable.class));
        verify(repository).countByCustomerId(customerId);
        verify(mapper).toResultList(orders);
    }

    @Test
    @DisplayName("Should list orders for customer with status filter")
    void testListOrders_CustomerWithStatus() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, false, 20, 0, "pending");
        Order pendingOrder = orders.get(0);
        Page<Order> page = new PageImpl<>(Arrays.asList(pendingOrder));

        when(repository.findByCustomerIdAndStatus(eq(customerId), eq(OrderStatus.PENDING), any(Pageable.class)))
                .thenReturn(page);
        when(repository.countByCustomerIdAndStatus(customerId, OrderStatus.PENDING)).thenReturn(1L);
        when(mapper.toResultList(anyList())).thenReturn(queryResults);

        OrderListQueryResult result = handler.handle(query);

        assertNotNull(result);
        assertEquals(1L, result.totalCount());

        verify(repository).findByCustomerIdAndStatus(eq(customerId), eq(OrderStatus.PENDING), any(Pageable.class));
        verify(repository).countByCustomerIdAndStatus(customerId, OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should list all orders for admin without status filter")
    void testListOrders_AdminNoStatus() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, true, 20, 0, null);
        Page<Order> page = new PageImpl<>(orders);

        when(repository.findAll(any(Pageable.class))).thenReturn(page);
        when(repository.count()).thenReturn(2L);
        when(mapper.toResultList(orders)).thenReturn(queryResults);

        OrderListQueryResult result = handler.handle(query);

        assertNotNull(result);
        assertEquals(2L, result.totalCount());

        verify(repository).findAll(any(Pageable.class));
        verify(repository).count();
        verify(repository, never()).findByCustomerId(any(), any());
    }

    @Test
    @DisplayName("Should list all orders for admin with status filter")
    void testListOrders_AdminWithStatus() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, true, 20, 0, "confirmed");
        Order confirmedOrder = orders.get(1);
        Page<Order> page = new PageImpl<>(Arrays.asList(confirmedOrder));

        when(repository.findByStatus(eq(OrderStatus.CONFIRMED), any(Pageable.class))).thenReturn(page);
        when(repository.count()).thenReturn(1L);
        when(mapper.toResultList(anyList())).thenReturn(queryResults);

        OrderListQueryResult result = handler.handle(query);

        assertNotNull(result);
        assertEquals(1L, result.totalCount());

        verify(repository).findByStatus(eq(OrderStatus.CONFIRMED), any(Pageable.class));
        verify(repository).count();
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testListOrders_Pagination() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, false, 10, 5, null);
        Page<Order> page = new PageImpl<>(orders);

        when(repository.findByCustomerId(eq(customerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(customerId)).thenReturn(15L);
        when(mapper.toResultList(orders)).thenReturn(queryResults);

        OrderListQueryResult result = handler.handle(query);

        assertEquals(10, result.limit());
        assertEquals(5, result.offset());
        assertEquals(15L, result.totalCount());

        verify(repository).findByCustomerId(eq(customerId), argThat(pageable ->
                pageable.getPageSize() == 10 && pageable.getOffset() == 0
        ));
    }

    @Test
    @DisplayName("Should use default values when limit and offset are null")
    void testListOrders_DefaultPagination() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, false, null, null, null);
        Page<Order> page = new PageImpl<>(orders);

        when(repository.findByCustomerId(eq(customerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(customerId)).thenReturn(2L);
        when(mapper.toResultList(orders)).thenReturn(queryResults);

        OrderListQueryResult result = handler.handle(query);

        assertEquals(20, result.limit());
        assertEquals(0, result.offset());

        verify(repository).findByCustomerId(eq(customerId), argThat(pageable ->
                pageable.getPageSize() == 20 && pageable.getOffset() == 0
        ));
    }

    @Test
    @DisplayName("Should limit maximum page size to 100")
    void testListOrders_MaxLimit() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, false, 200, 0, null);
        Page<Order> page = new PageImpl<>(orders);

        when(repository.findByCustomerId(eq(customerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(customerId)).thenReturn(2L);
        when(mapper.toResultList(orders)).thenReturn(queryResults);

        OrderListQueryResult result = handler.handle(query);

        assertEquals(100, result.limit());

        verify(repository).findByCustomerId(eq(customerId), argThat(pageable ->
                pageable.getPageSize() == 100
        ));
    }

    @Test
    @DisplayName("Should return empty list when no orders found")
    void testListOrders_EmptyResult() {
        ListOrdersQuery query = new ListOrdersQuery(customerId, false, 20, 0, null);
        Page<Order> page = new PageImpl<>(new ArrayList<>());

        when(repository.findByCustomerId(eq(customerId), any(Pageable.class))).thenReturn(page);
        when(repository.countByCustomerId(customerId)).thenReturn(0L);
        when(mapper.toResultList(anyList())).thenReturn(new ArrayList<>());

        OrderListQueryResult result = handler.handle(query);

        assertNotNull(result);
        assertEquals(0L, result.totalCount());
        assertTrue(result.data().isEmpty());
    }
}
