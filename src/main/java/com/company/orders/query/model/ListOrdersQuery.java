package com.company.orders.query.model;

import java.util.UUID;

public record ListOrdersQuery(
    UUID authenticatedCustomerId,
    boolean isAdmin,
    Integer limit,
    Integer offset,
    String status
) {}
