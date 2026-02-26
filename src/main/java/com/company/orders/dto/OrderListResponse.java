package com.company.orders.dto;

import java.util.List;

public record OrderListResponse(
    List<OrderDto> data,
    Long totalCount,
    Integer limit,
    Integer offset
) {}
