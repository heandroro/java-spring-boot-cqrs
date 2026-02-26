package com.company.orders.integration;

import com.company.orders.command.model.CreateOrderCommand;
import com.company.orders.shared.model.OrderItemDto;
import com.company.orders.command.repository.OrderCommandRepository;
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

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private RestClient restClient;

    @Autowired
    private OrderCommandRepository repository;

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
        
        OrderItemDto itemDto = new OrderItemDto(
            "p123",
            2,
            BigDecimal.valueOf(99.99),
            BigDecimal.valueOf(199.98)
        );

        CreateOrderCommand createCommand = new CreateOrderCommand(
            customerId,
            Arrays.asList(itemDto)
        );

        ResponseEntity<String> createResponse = restClient
            .post()
            .uri("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .body(createCommand)
            .retrieve()
            .toEntity(String.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        com.company.orders.command.model.CreateOrderResult createdOrder = objectMapper.readValue(createResponse.getBody(), com.company.orders.command.model.CreateOrderResult.class);
        assertEquals("PENDING", createdOrder.status());

        UUID orderId = createdOrder.orderId();
        assertNotNull(orderId);

        ResponseEntity<String> getResponse = restClient
            .get()
            .uri("/orders/{orderId}", orderId)
            .retrieve()
            .toEntity(String.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());

        com.company.orders.query.model.OrderQueryResult fetchedOrder = objectMapper.readValue(getResponse.getBody(), com.company.orders.query.model.OrderQueryResult.class);
        assertEquals(orderId, fetchedOrder.id());
        assertEquals("pending", fetchedOrder.status());
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

        com.company.orders.query.model.OrderListQueryResult parsed = objectMapper.readValue(response.getBody(), com.company.orders.query.model.OrderListQueryResult.class);
        assertEquals(20, parsed.limit());
        assertEquals(0, parsed.offset());
        assertNotNull(parsed.data());
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

        com.company.orders.query.model.OrderListQueryResult parsed = objectMapper.readValue(response.getBody(), com.company.orders.query.model.OrderListQueryResult.class);
        assertNotNull(parsed.data());
    }
}
