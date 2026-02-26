package com.company.orders.shared.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemDto(
    @NotBlank(message = "Product ID is required")
    String productId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    @NotNull(message = "Price per unit is required")
    @Min(value = 0, message = "Price per unit must be positive")
    BigDecimal pricePerUnit,

    BigDecimal subtotal
) {}
