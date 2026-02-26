package com.company.orders.service;

import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.enums.OrderStatus;
import com.company.orders.exception.ResourceNotFoundException;
import com.company.orders.mapper.OrderMapper;
import com.company.orders.entity.Order;
import com.company.orders.query.repository.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderQuery {

    private final OrderQueryRepository repository;
    private final OrderMapper mapper;
    private final OrderAuthorization authorization;

    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID orderId, UUID authenticatedCustomerId, boolean isAdmin) {
        log.info("Fetching order with id: {}", orderId);
        
        Order order = repository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        authorization.validateOrderAccess(order, authenticatedCustomerId, isAdmin);
        
        return mapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderListResponse listOrders(
            UUID authenticatedCustomerId,
            boolean isAdmin,
            Integer limit,
            Integer offset,
            String status) {
        
        log.info("Listing orders for customer: {}, limit: {}, offset: {}, status: {}", 
            authenticatedCustomerId, limit, offset, status);

        limit = Math.min(limit != null ? limit : 20, 100);
        offset = offset != null ? offset : 0;

        Pageable pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Order> page;
        long totalCount;

        if (isAdmin) {
            if (status != null) {
                OrderStatus orderStatus = OrderStatus.fromValue(status);
                page = repository.findByStatus(orderStatus, pageable);
                totalCount = repository.count();
            } else {
                page = repository.findAll(pageable);
                totalCount = repository.count();
            }
        } else {
            if (status != null) {
                OrderStatus orderStatus = OrderStatus.fromValue(status);
                page = repository.findByCustomerIdAndStatus(authenticatedCustomerId, orderStatus, pageable);
                totalCount = repository.countByCustomerIdAndStatus(authenticatedCustomerId, orderStatus);
            } else {
                page = repository.findByCustomerId(authenticatedCustomerId, pageable);
                totalCount = repository.countByCustomerId(authenticatedCustomerId);
            }
        }

        return new OrderListResponse(
            mapper.toDtoList(page.getContent()),
            totalCount,
            limit,
            offset
        );
    }
}
