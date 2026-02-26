package com.company.orders.mapper;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.model.Order;
import com.company.orders.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Order Mapper Tests")
class OrderMapperTest {

    @Autowired
    private OrderMapper mapper;

    private Order order;
    private OrderItem orderItem;
    private CreateOrderRequest createRequest;
    private OrderItemDto orderItemDto;

    @BeforeEach
    void setUp() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotal(BigDecimal.valueOf(199.98));
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedAt(OffsetDateTime.now());

        orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setProductId("p123");
        orderItem.setQuantity(2);
        orderItem.setPricePerUnit(BigDecimal.valueOf(99.99));
        orderItem.setSubtotal(BigDecimal.valueOf(199.98));
        orderItem.setCreatedAt(OffsetDateTime.now());
        order.addItem(orderItem);

        orderItemDto = new OrderItemDto();
        orderItemDto.setProductId("p456");
        orderItemDto.setQuantity(1);
        orderItemDto.setPricePerUnit(BigDecimal.valueOf(49.99));

        createRequest = new CreateOrderRequest();
        createRequest.setCustomerId(customerId);
        createRequest.setItems(Arrays.asList(orderItemDto));
    }

    @Test
    @DisplayName("Should map Order entity to OrderDto")
    void testToDto() {
        OrderDto dto = mapper.toDto(order);

        assertNotNull(dto);
        assertEquals(order.getId(), dto.getId());
        assertEquals(order.getCustomerId(), dto.getCustomerId());
        assertEquals("pending", dto.getStatus());
        assertEquals(order.getTotal(), dto.getTotal());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
    }

    @Test
    @DisplayName("Should map OrderItem entity to OrderItemDto")
    void testToItemDto() {
        OrderItemDto dto = mapper.toItemDto(orderItem);

        assertNotNull(dto);
        assertEquals(orderItem.getProductId(), dto.getProductId());
        assertEquals(orderItem.getQuantity(), dto.getQuantity());
        assertEquals(orderItem.getPricePerUnit(), dto.getPricePerUnit());
        assertEquals(orderItem.getSubtotal(), dto.getSubtotal());
    }

    @Test
    @DisplayName("Should map list of Order entities to list of OrderDto")
    void testToDtoList() {
        Order order2 = new Order();
        order2.setId(UUID.randomUUID());
        order2.setCustomerId(UUID.randomUUID());
        order2.setStatus(Order.OrderStatus.CONFIRMED);
        order2.setTotal(BigDecimal.valueOf(99.99));
        order2.setCreatedAt(OffsetDateTime.now());

        List<OrderDto> dtoList = mapper.toDtoList(Arrays.asList(order, order2));

        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals(order.getId(), dtoList.get(0).getId());
        assertEquals(order2.getId(), dtoList.get(1).getId());
    }

    @Test
    @DisplayName("Should map CreateOrderRequest to Order entity")
    void testToEntity() {
        Order entity = mapper.toEntity(createRequest);

        assertNotNull(entity);
        assertEquals(createRequest.getCustomerId(), entity.getCustomerId());
        assertEquals(Order.OrderStatus.PENDING, entity.getStatus());
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should map OrderItemDto to OrderItem entity")
    void testToItemEntity() {
        OrderItem entity = mapper.toItemEntity(orderItemDto);

        assertNotNull(entity);
        assertEquals(orderItemDto.getProductId(), entity.getProductId());
        assertEquals(orderItemDto.getQuantity(), entity.getQuantity());
        assertEquals(orderItemDto.getPricePerUnit(), entity.getPricePerUnit());
        assertNull(entity.getId());
        assertNull(entity.getOrder());
        assertNull(entity.getCreatedAt());
    }

    @Test
    @DisplayName("Should convert OrderStatus to string")
    void testStatusToString() {
        String status = mapper.statusToString(Order.OrderStatus.PENDING);
        assertEquals("pending", status);

        status = mapper.statusToString(Order.OrderStatus.CONFIRMED);
        assertEquals("confirmed", status);

        status = mapper.statusToString(Order.OrderStatus.SHIPPED);
        assertEquals("shipped", status);

        status = mapper.statusToString(Order.OrderStatus.DELIVERED);
        assertEquals("delivered", status);
    }

    @Test
    @DisplayName("Should return null when status is null")
    void testStatusToString_WithNullStatus() {
        String status = mapper.statusToString(null);
        assertNull(status);
    }

    @Test
    @DisplayName("Should handle empty list in toDtoList")
    void testToDtoList_WithEmptyList() {
        List<OrderDto> dtoList = mapper.toDtoList(Arrays.asList());

        assertNotNull(dtoList);
        assertTrue(dtoList.isEmpty());
    }

    @Test
    @DisplayName("Should map Order with null status")
    void testToDto_WithNullStatus() {
        order.setStatus(null);
        OrderDto dto = mapper.toDto(order);

        assertNotNull(dto);
        assertNull(dto.getStatus());
    }
}
