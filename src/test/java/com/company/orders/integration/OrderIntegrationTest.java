package com.company.orders.integration;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "server.port=0"  // Random port assignment
})
@Transactional
@DisplayName("Order Integration Tests")
class OrderIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private RestClient restClient;

    @Autowired
    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        restClient = RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .build();
    }

    @Test
    @DisplayName("Should create and retrieve order in full lifecycle")
    void testFullOrderLifecycle() throws Exception {
        UUID customerId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setProductId("p123");
        itemDto.setQuantity(2);
        itemDto.setPricePerUnit(BigDecimal.valueOf(99.99));

        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setCustomerId(customerId);
        createRequest.setItems(Arrays.asList(itemDto));

        ResponseEntity<String> createResponse = restClient
            .post()
            .uri("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createRequest)
            .retrieve()
            .toEntity(String.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        OrderDto createdOrder = objectMapper.readValue(createResponse.getBody(), OrderDto.class);
        assertEquals("pending", createdOrder.getStatus());

        UUID orderId = createdOrder.getId();
        assertNotNull(orderId);

        ResponseEntity<String> getResponse = restClient
            .get()
            .uri("/orders/{orderId}", orderId)
            .retrieve()
            .toEntity(String.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());

        OrderDto fetchedOrder = objectMapper.readValue(getResponse.getBody(), OrderDto.class);
        assertEquals(orderId, fetchedOrder.getId());
        assertEquals("pending", fetchedOrder.getStatus());
    }

    @Test
    @DisplayName("Should list orders with pagination")
    void testListOrdersWithPagination() throws Exception {
        ResponseEntity<String> response = restClient
            .get()
            .uri("/orders?limit=20&offset=0")
            .retrieve()
            .toEntity(String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        OrderListResponse parsed = objectMapper.readValue(response.getBody(), OrderListResponse.class);
        assertEquals(20, parsed.getLimit());
        assertEquals(0, parsed.getOffset());
        assertNotNull(parsed.getData());
    }

    @Test
    @DisplayName("Should filter orders by status")
    void testFilterOrdersByStatus() throws Exception {
        ResponseEntity<String> response = restClient
            .get()
            .uri("/orders?status=pending&limit=20")
            .retrieve()
            .toEntity(String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        OrderListResponse parsed = objectMapper.readValue(response.getBody(), OrderListResponse.class);
        assertNotNull(parsed.getData());
    }
}
