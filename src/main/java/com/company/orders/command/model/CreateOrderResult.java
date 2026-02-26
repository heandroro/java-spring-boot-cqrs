package com.company.orders.command.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateOrderResult(
    UUID orderId,
    UUID customerId,
    String status,
    BigDecimal total,
    OffsetDateTime createdAt
) {}
