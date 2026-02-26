package com.company.orders.query.model;

import java.util.List;

public record OrderListQueryResult(
    List<OrderQueryResult> data,
    long totalCount,
    int limit,
    int offset
) {}
