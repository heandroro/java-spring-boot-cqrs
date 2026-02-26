package com.company.orders.query.model;

import java.util.UUID;

public record GetOrderQuery(
    UUID orderId,
    UUID authenticatedCustomerId,
    boolean isAdmin
) {}
