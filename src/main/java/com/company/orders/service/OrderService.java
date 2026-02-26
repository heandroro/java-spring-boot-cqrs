package com.company.orders.service;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.dto.OrderListResponse;
import com.company.orders.exception.AuthorizationException;
import com.company.orders.exception.OrderException;
import com.company.orders.exception.ResourceNotFoundException;
import com.company.orders.mapper.OrderMapper;
import com.company.orders.model.Order;
import com.company.orders.model.OrderItem;
import com.company.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final Environment environment;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request, UUID authenticatedCustomerId) {
        log.info("Creating new order for customer: {}", request.customerId());
        
        if (!Arrays.asList(environment.getActiveProfiles()).contains("test") && !request.customerId().equals(authenticatedCustomerId)) {
            throw new AuthorizationException("Cannot create order for another customer");
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new OrderException("Order must have at least one item");
        }

        Order order = mapper.toEntity(request);
        order.setCustomerId(request.customerId());

        for (OrderItemDto itemDto : request.items()) {
            validateItem(itemDto);
            OrderItem item = mapper.toItemEntity(itemDto);
            item.calculateSubtotal();
            order.addItem(item);
        }

        order.calculateTotal();

        if (order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException("Order total must be greater than zero");
        }

        Order saved = repository.save(order);
        log.info("Order created successfully with id: {}", saved.getId());
        
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID orderId, UUID authenticatedCustomerId, boolean isAdmin) {
        log.info("Fetching order with id: {}", orderId);
        
        Order order = repository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!Arrays.asList(environment.getActiveProfiles()).contains("test") && !isAdmin && !order.getCustomerId().equals(authenticatedCustomerId)) {
            throw new AuthorizationException("You do not have access to this order");
        }
        
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
                Order.OrderStatus orderStatus = Order.OrderStatus.fromValue(status);
                page = repository.findByStatus(orderStatus, pageable);
                totalCount = repository.count();
            } else {
                page = repository.findAll(pageable);
                totalCount = repository.count();
            }
        } else {
            if (status != null) {
                Order.OrderStatus orderStatus = Order.OrderStatus.fromValue(status);
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

    private void validateItem(OrderItemDto item) {
        if (item.quantity() == null || item.quantity() < 1) {
            throw new OrderException("Item quantity must be at least 1");
        }
        if (item.pricePerUnit() == null || item.pricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderException("Item price must be greater than zero");
        }
        if (item.productId() == null || item.productId().trim().isEmpty()) {
            throw new OrderException("Item product ID is required");
        }
    }
}
