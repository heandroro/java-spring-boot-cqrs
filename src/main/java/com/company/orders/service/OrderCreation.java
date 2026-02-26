package com.company.orders.service;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.exception.OrderException;
import com.company.orders.mapper.OrderMapper;
import com.company.orders.entity.Order;
import com.company.orders.entity.OrderItem;
import com.company.orders.command.repository.OrderCommandRepository;
import com.company.orders.command.service.OrderValidator;
import com.company.orders.command.service.OrderAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreation {

    private final OrderCommandRepository repository;
    private final OrderMapper mapper;
    private final OrderValidator validator;
    private final OrderAuthorization authorization;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request, UUID authenticatedCustomerId) {
        log.info("Creating new order for customer: {}", request.customerId());
        
        authorization.validateCreateOrderAuthorization(request.customerId(), authenticatedCustomerId);

        if (request.items() == null || request.items().isEmpty()) {
            throw new OrderException("Order must have at least one item");
        }

        Order order = mapper.toEntity(request);
        order.setCustomerId(request.customerId());

        for (OrderItemDto itemDto : request.items()) {
            validator.validateItem(itemDto);
            OrderItem item = mapper.toItemEntity(itemDto);
            item.calculateSubtotal();
            order.addItem(item);
        }

        order.calculateTotal();
        validator.validateOrderTotal(order.getTotal());

        Order saved = repository.save(order);
        log.info("Order created successfully with id: {}", saved.getId());
        
        return mapper.toDto(saved);
    }
}
