package com.company.orders.controller;

import com.company.orders.exception.GlobalExceptionHandler;
import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.service.OrderCreation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Creation Controller Tests")
class OrderCreationControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderCreation orderCreation;

    private OrderDto orderDto;
    private CreateOrderRequest createRequest;
    private UUID testOrderId;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        OrderCreationController controller = new OrderCreationController(orderCreation);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();

        testOrderId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        
        orderDto = new OrderDto();
        orderDto.setId(testOrderId);
        orderDto.setCustomerId(testCustomerId);
        orderDto.setStatus("pending");
        orderDto.setTotal(BigDecimal.valueOf(249.97));

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
    @DisplayName("Should create order and return 201 status")
    void testCreateOrder_ShouldReturnStatus201() throws Exception {
        when(orderCreation.createOrder(any(CreateOrderRequest.class), any(UUID.class))).thenReturn(orderDto);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void testCreateOrder_ShouldReturnStatus400_WhenValidationFails() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest(null, Arrays.asList());

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle null authentication")
    void testCreateOrder_WithNullAuthentication() throws Exception {
        when(orderCreation.createOrder(any(CreateOrderRequest.class), any(UUID.class))).thenReturn(orderDto);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should handle authentication with valid UUID principal")
    void testCreateOrder_WithValidUUIDPrincipal() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(testCustomerId.toString());
        when(auth.getPrincipal()).thenReturn(testCustomerId.toString());
        when(orderCreation.createOrder(any(CreateOrderRequest.class), eq(testCustomerId))).thenReturn(orderDto);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .principal(auth))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should handle authentication with invalid UUID principal")
    void testCreateOrder_WithInvalidUUIDPrincipal() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("not-a-uuid");
        when(auth.getPrincipal()).thenReturn("not-a-uuid");
        when(orderCreation.createOrder(any(CreateOrderRequest.class), any(UUID.class))).thenReturn(orderDto);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .principal(auth))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should handle authentication with null principal")
    void testCreateOrder_WithNullPrincipal() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(null);
        when(orderCreation.createOrder(any(CreateOrderRequest.class), any(UUID.class))).thenReturn(orderDto);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .principal(auth))
                .andExpect(status().isCreated());
    }
}
