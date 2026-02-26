package com.company.orders.query.handler;

import com.company.orders.command.service.OrderAuthorization;
import com.company.orders.domain.entity.Order;
import com.company.orders.shared.exception.ResourceNotFoundException;
import com.company.orders.query.model.GetOrderQuery;
import com.company.orders.query.model.OrderQueryResult;
import com.company.orders.query.repository.OrderQueryRepository;
import com.company.orders.shared.mapper.OrderQueryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetOrderQueryHandler {

    private final OrderQueryRepository repository;
    private final OrderQueryMapper mapper;
    private final OrderAuthorization authorization;

    @Transactional(readOnly = true)
    public OrderQueryResult handle(GetOrderQuery query) {
        log.info("Fetching order with id: {}", query.orderId());
        
        Order order = repository.findByIdWithItems(query.orderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + query.orderId()));

        authorization.validateOrderAccess(order, query.authenticatedCustomerId(), query.isAdmin());
        
        return mapper.toResult(order);
    }
}
