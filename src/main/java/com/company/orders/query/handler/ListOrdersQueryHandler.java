package com.company.orders.query.handler;

import com.company.orders.domain.entity.Order;
import com.company.orders.domain.enums.OrderStatus;
import com.company.orders.query.model.ListOrdersQuery;
import com.company.orders.query.model.OrderListQueryResult;
import com.company.orders.query.repository.OrderQueryRepository;
import com.company.orders.shared.mapper.OrderQueryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListOrdersQueryHandler {

    private final OrderQueryRepository repository;
    private final OrderQueryMapper mapper;

    @Transactional(readOnly = true)
    public OrderListQueryResult handle(ListOrdersQuery query) {
        log.info("Listing orders for customer: {}, limit: {}, offset: {}, status: {}", 
            query.authenticatedCustomerId(), query.limit(), query.offset(), query.status());

        int limit = Math.min(query.limit() != null ? query.limit() : 20, 100);
        int offset = query.offset() != null ? query.offset() : 0;

        Pageable pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Order> page;
        long totalCount;

        if (query.isAdmin()) {
            if (query.status() != null) {
                OrderStatus orderStatus = OrderStatus.fromValue(query.status());
                page = repository.findByStatus(orderStatus, pageable);
                totalCount = repository.count();
            } else {
                page = repository.findAll(pageable);
                totalCount = repository.count();
            }
        } else {
            if (query.status() != null) {
                OrderStatus orderStatus = OrderStatus.fromValue(query.status());
                page = repository.findByCustomerIdAndStatus(query.authenticatedCustomerId(), orderStatus, pageable);
                totalCount = repository.countByCustomerIdAndStatus(query.authenticatedCustomerId(), orderStatus);
            } else {
                page = repository.findByCustomerId(query.authenticatedCustomerId(), pageable);
                totalCount = repository.countByCustomerId(query.authenticatedCustomerId());
            }
        }

        return new OrderListQueryResult(
            mapper.toResultList(page.getContent()),
            totalCount,
            limit,
            offset
        );
    }
}
