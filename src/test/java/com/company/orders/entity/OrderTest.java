package com.company.orders.entity;

import com.company.orders.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Model Tests")
class OrderTest {

    @Test
    @DisplayName("Should create Order with default values")
    void createOrderWithDefaults() {
        Order order = new Order();
        
        assertNull(order.getId());
        assertNull(order.getCustomerId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(BigDecimal.ZERO, order.getTotal());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
        assertNull(order.getCreatedAt());
        assertNull(order.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create Order with all args constructor")
    void createOrderWithAllArgs() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        Order order = new Order(
            id,
            customerId,
            OrderStatus.CONFIRMED,
            new BigDecimal("100.00"),
            null,
            null,
            null
        );
        
        assertEquals(id, order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(new BigDecimal("100.00"), order.getTotal());
    }

    @Test
    @DisplayName("Should add item to order")
    void addItemToOrder() {
        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setProductId("prod-123");
        item.setQuantity(2);
        item.setPricePerUnit(new BigDecimal("50.00"));
        item.calculateSubtotal();
        
        order.addItem(item);
        
        assertEquals(1, order.getItems().size());
        assertTrue(order.getItems().contains(item));
        assertEquals(order, item.getOrder());
    }

    @Test
    @DisplayName("Should remove item from order")
    void removeItemFromOrder() {
        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setProductId("prod-123");
        
        order.addItem(item);
        assertEquals(1, order.getItems().size());
        
        order.removeItem(item);
        
        assertEquals(0, order.getItems().size());
        assertNull(item.getOrder());
    }

    @Test
    @DisplayName("Should calculate total from items")
    void calculateTotal() {
        Order order = new Order();
        
        OrderItem item1 = new OrderItem();
        item1.setQuantity(2);
        item1.setPricePerUnit(new BigDecimal("50.00"));
        item1.calculateSubtotal();
        order.addItem(item1);
        
        OrderItem item2 = new OrderItem();
        item2.setQuantity(1);
        item2.setPricePerUnit(new BigDecimal("30.00"));
        item2.calculateSubtotal();
        order.addItem(item2);
        
        order.calculateTotal();
        
        assertEquals(new BigDecimal("130.00"), order.getTotal());
    }

    @Test
    @DisplayName("Should calculate total as zero when no items")
    void calculateTotalWithNoItems() {
        Order order = new Order();
        
        order.calculateTotal();
        
        assertEquals(BigDecimal.ZERO, order.getTotal());
    }

    @Test
    @DisplayName("Should get OrderStatus value")
    void getOrderStatusValue() {
        assertEquals("pending", OrderStatus.PENDING.getValue());
        assertEquals("confirmed", OrderStatus.CONFIRMED.getValue());
        assertEquals("shipped", OrderStatus.SHIPPED.getValue());
        assertEquals("delivered", OrderStatus.DELIVERED.getValue());
    }

    @Test
    @DisplayName("Should get OrderStatus from value")
    void getOrderStatusFromValue() {
        assertEquals(OrderStatus.PENDING, OrderStatus.fromValue("pending"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.fromValue("confirmed"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.fromValue("shipped"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.fromValue("delivered"));
    }

    @Test
    @DisplayName("Should throw exception for invalid OrderStatus value")
    void getOrderStatusFromInvalidValue() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> OrderStatus.fromValue("invalid")
        );
        
        assertEquals("Invalid status: invalid", exception.getMessage());
    }

    @Test
    @DisplayName("Should verify all OrderStatus enum values")
    void verifyOrderStatusEnumValues() {
        OrderStatus[] statuses = OrderStatus.values();
        
        assertEquals(4, statuses.length);
        assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("PENDING"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.valueOf("CONFIRMED"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.valueOf("SHIPPED"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.valueOf("DELIVERED"));
    }

    @Test
    @DisplayName("Should set and get all Order fields")
    void setAndGetAllFields() {
        Order order = new Order();
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        order.setId(id);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.SHIPPED);
        order.setTotal(new BigDecimal("200.00"));
        
        assertEquals(id, order.getId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(new BigDecimal("200.00"), order.getTotal());
    }
}
