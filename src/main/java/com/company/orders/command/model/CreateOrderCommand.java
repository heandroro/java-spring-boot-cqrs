package com.company.orders.command.model;

import com.company.orders.dto.OrderItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
    @NotNull(message = "Customer ID is required")
    UUID customerId,

    @NotEmpty(message = "Items cannot be empty")
    @Valid
    List<OrderItemDto> items
) {}
