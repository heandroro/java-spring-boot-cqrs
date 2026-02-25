package com.company.orders.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

    private List<OrderDto> data;
    private Long totalCount;
    private Integer limit;
    private Integer offset;
}
