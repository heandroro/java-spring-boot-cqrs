package com.company.orders.controller;

import com.company.orders.exception.GlobalExceptionHandler;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.service.OrderQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Query Controller Tests")
class OrderQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderQuery orderQuery;

    private OrderDto orderDto;
    private UUID testOrderId;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        OrderQueryController controller = new OrderQueryController(orderQuery);

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        testOrderId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        
        orderDto = new OrderDto();
        orderDto.setId(testOrderId);
        orderDto.setCustomerId(testCustomerId);
        orderDto.setStatus("pending");
        orderDto.setTotal(BigDecimal.valueOf(249.97));
    }

    @Test
    @DisplayName("Should get order by ID and return 200 status")
    void testGetOrder_ShouldReturnStatus200() throws Exception {
        when(orderQuery.getOrder(eq(testOrderId), any(UUID.class), any(Boolean.class))).thenReturn(orderDto);

        mockMvc.perform(get("/orders/{orderId}", testOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    @DisplayName("Should list orders and return 200 status")
    void testListOrders_ShouldReturnStatus200() throws Exception {
        OrderListResponse response = new OrderListResponse(
            Arrays.asList(orderDto),
            1L,
            20,
            0
        );
        when(orderQuery.listOrders(any(UUID.class), any(Boolean.class), any(Integer.class), 
            any(Integer.class), any())).thenReturn(response);

        mockMvc.perform(get("/orders")
                .param("limit", "20")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @DisplayName("Should list orders with status filter")
    void testListOrders_WithStatusFilter() throws Exception {
        OrderListResponse response = new OrderListResponse(
            Arrays.asList(orderDto),
            1L,
            20,
            0
        );
        when(orderQuery.listOrders(any(UUID.class), any(Boolean.class), any(Integer.class), 
            any(Integer.class), eq("pending"))).thenReturn(response);

        mockMvc.perform(get("/orders")
                .param("limit", "20")
                .param("offset", "0")
                .param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @DisplayName("Should detect admin role with ROLE_ADMIN")
    void testListOrders_WithAdminRole() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(testCustomerId.toString());
        when(auth.getPrincipal()).thenReturn(testCustomerId.toString());
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(auth.getAuthorities()).thenReturn((Collection) authorities);

        OrderListResponse response = new OrderListResponse(
            Arrays.asList(orderDto),
            1L,
            20,
            0
        );
        when(orderQuery.listOrders(any(UUID.class), eq(true), any(Integer.class), 
            any(Integer.class), any())).thenReturn(response);

        mockMvc.perform(get("/orders")
                .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should detect admin role with lowercase admin")
    void testListOrders_WithLowercaseAdminRole() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(testCustomerId.toString());
        when(auth.getPrincipal()).thenReturn(testCustomerId.toString());
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("admin")
        );
        when(auth.getAuthorities()).thenReturn((Collection) authorities);

        OrderListResponse response = new OrderListResponse(
            Arrays.asList(orderDto),
            1L,
            20,
            0
        );
        when(orderQuery.listOrders(any(UUID.class), eq(true), any(Integer.class), 
            any(Integer.class), any())).thenReturn(response);

        mockMvc.perform(get("/orders")
                .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should not detect admin role with regular user")
    void testListOrders_WithRegularUser() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(testCustomerId.toString());
        when(auth.getPrincipal()).thenReturn(testCustomerId.toString());
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        when(auth.getAuthorities()).thenReturn((Collection) authorities);

        OrderListResponse response = new OrderListResponse(
            Arrays.asList(orderDto),
            1L,
            20,
            0
        );
        when(orderQuery.listOrders(any(UUID.class), eq(false), any(Integer.class), 
            any(Integer.class), any())).thenReturn(response);

        mockMvc.perform(get("/orders")
                .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle invalid UUID with fallback")
    void testListOrders_WithInvalidUUID() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("not-a-uuid");
        when(auth.getPrincipal()).thenReturn("not-a-uuid");
        when(auth.getAuthorities()).thenReturn((Collection) Collections.emptyList());

        OrderListResponse response = new OrderListResponse(
            Arrays.asList(orderDto),
            1L,
            20,
            0
        );
        when(orderQuery.listOrders(any(UUID.class), eq(false), any(Integer.class), 
            any(Integer.class), any())).thenReturn(response);

        mockMvc.perform(get("/orders")
                .principal(auth))
                .andExpect(status().isOk());
    }
}
