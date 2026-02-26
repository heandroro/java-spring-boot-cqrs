package com.company.orders.command.service;

import com.company.orders.dto.OrderItemDto;
import com.company.orders.shared.exception.OrderException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderValidator {

    public void validateItem(OrderItemDto item) {
        if (item.quantity() == null || item.quantity() < 1) {
            throw new OrderException("Item quantity must be at least 1");
        }
        if (item.pricePerUnit() == null || item.pricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException("Item price must be greater than zero");
        }
        if (item.productId() == null || item.productId().trim().isEmpty()) {
            throw new OrderException("Item product ID is required");
        }
    }

    public void validateOrderTotal(BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException("Order total must be greater than zero");
        }
    }
}
