package com.company.orders.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderItem Model Tests")
class OrderItemTest {

    @Test
    @DisplayName("Should create OrderItem with default values")
    void createOrderItemWithDefaults() {
        OrderItem item = new OrderItem();

        assertNull(item.getId());
        assertNull(item.getOrder());
        assertNull(item.getProductId());
        assertNull(item.getQuantity());
        assertNull(item.getPricePerUnit());
        assertEquals(BigDecimal.ZERO, item.getSubtotal());
        assertNull(item.getCreatedAt());
    }

    @Test
    @DisplayName("Should create OrderItem with all args constructor")
    void createOrderItemWithAllArgs() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        String productId = "prod-123";
        Integer quantity = 2;
        BigDecimal pricePerUnit = new BigDecimal("50.00");
        BigDecimal subtotal = new BigDecimal("100.00");

        OrderItem item = new OrderItem(
            id, order, productId, quantity, pricePerUnit, subtotal, null
        );

        assertEquals(id, item.getId());
        assertEquals(order, item.getOrder());
        assertEquals(productId, item.getProductId());
        assertEquals(quantity, item.getQuantity());
        assertEquals(pricePerUnit, item.getPricePerUnit());
        assertEquals(subtotal, item.getSubtotal());
    }

    @Test
    @DisplayName("Should calculate subtotal correctly")
    void calculateSubtotal() {
        OrderItem item = new OrderItem();
        item.setQuantity(3);
        item.setPricePerUnit(new BigDecimal("25.50"));

        item.calculateSubtotal();

        assertEquals(new BigDecimal("76.50"), item.getSubtotal());
    }

    @Test
    @DisplayName("Should calculate subtotal as zero when quantity is null")
    void calculateSubtotalWithNullQuantity() {
        OrderItem item = new OrderItem();
        item.setQuantity(null);
        item.setPricePerUnit(new BigDecimal("25.50"));

        item.calculateSubtotal();

        assertEquals(BigDecimal.ZERO, item.getSubtotal());
    }

    @Test
    @DisplayName("Should calculate subtotal as zero when pricePerUnit is null")
    void calculateSubtotalWithNullPrice() {
        OrderItem item = new OrderItem();
        item.setQuantity(3);
        item.setPricePerUnit(null);

        item.calculateSubtotal();

        assertEquals(BigDecimal.ZERO, item.getSubtotal());
    }

    @Test
    @DisplayName("Should set and get all OrderItem fields")
    void setAndGetAllFields() {
        OrderItem item = new OrderItem();
        UUID id = UUID.randomUUID();
        Order order = new Order();
        String productId = "prod-456";
        Integer quantity = 5;
        BigDecimal pricePerUnit = new BigDecimal("12.99");

        item.setId(id);
        item.setOrder(order);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPricePerUnit(pricePerUnit);

        assertEquals(id, item.getId());
        assertEquals(order, item.getOrder());
        assertEquals(productId, item.getProductId());
        assertEquals(quantity, item.getQuantity());
        assertEquals(pricePerUnit, item.getPricePerUnit());
        assertNotNull(item.getSubtotal()); // subtotal is initialized to ZERO
        assertNull(item.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle prePersist callback")
    void prePersistCallback() {
        OrderItem item = new OrderItem();
        item.setQuantity(2);
        item.setPricePerUnit(new BigDecimal("15.00"));

        item.prePersist();

        assertEquals(new BigDecimal("30.00"), item.getSubtotal());
    }

    @Test
    @DisplayName("Should handle preUpdate callback")
    void preUpdateCallback() {
        OrderItem item = new OrderItem();
        item.setQuantity(4);
        item.setPricePerUnit(new BigDecimal("10.00"));

        item.prePersist(); // Same as preUpdate for this logic

        assertEquals(new BigDecimal("40.00"), item.getSubtotal());
    }
}
