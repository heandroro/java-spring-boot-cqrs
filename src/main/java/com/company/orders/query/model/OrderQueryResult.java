package com.company.orders.query.model;

import com.company.orders.dto.OrderItemDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderQueryResult(
    UUID id,
    UUID customerId,
    String status,
    BigDecimal total,
    List<OrderItemDto> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
