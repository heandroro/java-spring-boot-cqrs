package com.company.orders.controller;

import com.company.orders.exception.GlobalExceptionHandler;
import com.company.orders.model.CreateOrderRequest;
import com.company.orders.model.OrderDto;
import com.company.orders.model.OrderItemDto;
import com.company.orders.model.OrderListResponse;
import com.company.orders.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Controller Tests")
class OrderControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService service;

    private OrderDto orderDto;
    private CreateOrderRequest createRequest;
    private UUID testOrderId;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(service);

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

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId("p123");
        itemDto.setQuantity(2);
        itemDto.setPricePerUnit(BigDecimal.valueOf(99.99));

        createRequest = new CreateOrderRequest();
        createRequest.setCustomerId(testCustomerId);
        createRequest.setItems(Arrays.asList(itemDto));
    }

    @Test
    @DisplayName("Should create order and return 201 status")
    void testCreateOrder_ShouldReturnStatus201() throws Exception {
        when(service.createOrder(any(CreateOrderRequest.class), any(UUID.class))).thenReturn(orderDto);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    @DisplayName("Should get order by ID and return 200 status")
    void testGetOrder_ShouldReturnStatus200() throws Exception {
        when(service.getOrder(eq(testOrderId), any(UUID.class), any(Boolean.class))).thenReturn(orderDto);

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
        when(service.listOrders(any(UUID.class), any(Boolean.class), any(Integer.class), 
            any(Integer.class), any())).thenReturn(response);

        mockMvc.perform(get("/orders")
                .param("limit", "20")
                .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(testOrderId.toString()))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void testCreateOrder_ShouldReturnStatus400_WhenValidationFails() throws Exception {
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        invalidRequest.setCustomerId(null);
        invalidRequest.setItems(Arrays.asList());

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
