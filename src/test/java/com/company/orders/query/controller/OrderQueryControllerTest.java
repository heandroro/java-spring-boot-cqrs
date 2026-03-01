package com.company.orders.query.controller;

import com.company.orders.query.handler.GetOrderQueryHandler;
import com.company.orders.query.handler.ListOrdersQueryHandler;
import com.company.orders.query.model.GetOrderQuery;
import com.company.orders.query.model.ListOrdersQuery;
import com.company.orders.query.model.OrderListQueryResult;
import com.company.orders.query.model.OrderQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderQueryController Tests")
class OrderQueryControllerTest {

    @Mock
    private GetOrderQueryHandler getOrderHandler;

    @Mock
    private ListOrdersQueryHandler listOrdersHandler;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderQueryController controller;

    private UUID orderId;
    private UUID customerId;
    private OrderQueryResult orderQueryResult;
    private OrderListQueryResult orderListQueryResult;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        orderQueryResult = new OrderQueryResult(
            orderId,
            customerId,
            "pending",
            BigDecimal.valueOf(100.00),
            new ArrayList<>(),
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );

        orderListQueryResult = new OrderListQueryResult(
            new ArrayList<>(),
            0L,
            20,
            0
        );
    }

    @Test
    @DisplayName("Should extract customer ID from valid UUID principal")
    void testExtractCustomerId_ValidUUID() {
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());

        when(getOrderHandler.handle(any(GetOrderQuery.class))).thenReturn(orderQueryResult);

        ResponseEntity<OrderQueryResult> response = controller.getOrder(orderId, authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(getOrderHandler).handle(argThat(query -> 
            query.orderId().equals(orderId) && query.authenticatedCustomerId().equals(customerId)
        ));
    }

    @Test
    @DisplayName("Should generate random UUID when authentication is null")
    void testExtractCustomerId_NullAuthentication() {
        when(getOrderHandler.handle(any(GetOrderQuery.class))).thenReturn(orderQueryResult);

        ResponseEntity<OrderQueryResult> response = controller.getOrder(orderId, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(getOrderHandler).handle(argThat(query -> 
            query.orderId().equals(orderId) && query.authenticatedCustomerId() != null
        ));
    }

    @Test
    @DisplayName("Should generate random UUID when principal is null")
    void testExtractCustomerId_NullPrincipal() {
        when(authentication.getPrincipal()).thenReturn(null);
        when(getOrderHandler.handle(any(GetOrderQuery.class))).thenReturn(orderQueryResult);

        ResponseEntity<OrderQueryResult> response = controller.getOrder(orderId, authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(getOrderHandler).handle(argThat(query -> 
            query.orderId().equals(orderId) && query.authenticatedCustomerId() != null
        ));
    }

    @Test
    @DisplayName("Should generate random UUID when principal is not a valid UUID")
    void testExtractCustomerId_InvalidUUID() {
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn("not-a-uuid");
        when(getOrderHandler.handle(any(GetOrderQuery.class))).thenReturn(orderQueryResult);

        ResponseEntity<OrderQueryResult> response = controller.getOrder(orderId, authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(getOrderHandler).handle(argThat(query -> 
            query.orderId().equals(orderId) && query.authenticatedCustomerId() != null
        ));
    }

    @Test
    @DisplayName("Should return false when authentication is null for isAdmin")
    void testIsAdmin_NullAuthentication() {
        when(listOrdersHandler.handle(any(ListOrdersQuery.class))).thenReturn(orderListQueryResult);

        ResponseEntity<OrderListQueryResult> response = controller.listOrders(20, 0, null, null);

        assertNotNull(response);
        verify(listOrdersHandler).handle(argThat(query -> !query.isAdmin()));
    }

    @Test
    @DisplayName("Should return true when user has ROLE_ADMIN")
    void testIsAdmin_WithRoleAdmin() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        doReturn(authorities).when(authentication).getAuthorities();
        when(listOrdersHandler.handle(any(ListOrdersQuery.class))).thenReturn(orderListQueryResult);

        ResponseEntity<OrderListQueryResult> response = controller.listOrders(20, 0, null, authentication);

        assertNotNull(response);
        verify(listOrdersHandler).handle(argThat(query -> query.isAdmin()));
    }

    @Test
    @DisplayName("Should return true when user has admin authority")
    void testIsAdmin_WithAdminAuthority() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("admin"));
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        doReturn(authorities).when(authentication).getAuthorities();
        when(listOrdersHandler.handle(any(ListOrdersQuery.class))).thenReturn(orderListQueryResult);

        ResponseEntity<OrderListQueryResult> response = controller.listOrders(20, 0, null, authentication);

        assertNotNull(response);
        verify(listOrdersHandler).handle(argThat(query -> query.isAdmin()));
    }

    @Test
    @DisplayName("Should return false when user has no admin authority")
    void testIsAdmin_WithoutAdminAuthority() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        doReturn(authorities).when(authentication).getAuthorities();
        when(listOrdersHandler.handle(any(ListOrdersQuery.class))).thenReturn(orderListQueryResult);

        ResponseEntity<OrderListQueryResult> response = controller.listOrders(20, 0, null, authentication);

        assertNotNull(response);
        verify(listOrdersHandler).handle(argThat(query -> !query.isAdmin()));
    }

    @Test
    @DisplayName("Should list orders with default parameters")
    void testListOrders_WithDefaults() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        doReturn(authorities).when(authentication).getAuthorities();
        when(listOrdersHandler.handle(any(ListOrdersQuery.class))).thenReturn(orderListQueryResult);

        ResponseEntity<OrderListQueryResult> response = controller.listOrders(20, 0, null, authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(listOrdersHandler).handle(argThat(query -> 
            query.limit() == 20 && query.offset() == 0 && query.status() == null
        ));
    }

    @Test
    @DisplayName("Should list orders with status filter")
    void testListOrders_WithStatusFilter() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        doReturn(authorities).when(authentication).getAuthorities();
        when(listOrdersHandler.handle(any(ListOrdersQuery.class))).thenReturn(orderListQueryResult);

        ResponseEntity<OrderListQueryResult> response = controller.listOrders(10, 5, "pending", authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(listOrdersHandler).handle(argThat(query -> 
            query.limit() == 10 && query.offset() == 5 && "pending".equals(query.status())
        ));
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void testGetOrder_Success() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        when(authentication.getPrincipal()).thenReturn("valid");
        when(authentication.getName()).thenReturn(customerId.toString());
        doReturn(authorities).when(authentication).getAuthorities();
        when(getOrderHandler.handle(any(GetOrderQuery.class))).thenReturn(orderQueryResult);

        ResponseEntity<OrderQueryResult> response = controller.getOrder(orderId, authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(orderQueryResult, response.getBody());
        verify(getOrderHandler).handle(argThat(query -> 
            query.orderId().equals(orderId) && query.authenticatedCustomerId().equals(customerId)
        ));
    }
}
