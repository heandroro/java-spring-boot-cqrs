package com.company.orders.service;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCreation orderCreation;
    private final OrderQuery orderQuery;

    public OrderDto createOrder(CreateOrderRequest request, UUID authenticatedCustomerId) {
        return orderCreation.createOrder(request, authenticatedCustomerId);
    }

    public OrderDto getOrder(UUID orderId, UUID authenticatedCustomerId, boolean isAdmin) {
        return orderQuery.getOrder(orderId, authenticatedCustomerId, isAdmin);
    }

    public OrderListResponse listOrders(
            UUID authenticatedCustomerId,
            boolean isAdmin,
            Integer limit,
            Integer offset,
            String status) {
        return orderQuery.listOrders(authenticatedCustomerId, isAdmin, limit, offset, status);
    }
}
