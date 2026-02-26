package com.company.orders.command.handler;

import com.company.orders.command.model.CreateOrderCommand;
import com.company.orders.command.model.CreateOrderResult;
import com.company.orders.command.repository.OrderCommandRepository;
import com.company.orders.command.service.OrderAuthorization;
import com.company.orders.command.service.OrderValidator;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.entity.Order;
import com.company.orders.entity.OrderItem;
import com.company.orders.exception.OrderException;
import com.company.orders.shared.mapper.OrderCommandMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateOrderCommandHandler {

    private final OrderCommandRepository repository;
    private final OrderCommandMapper mapper;
    private final OrderValidator validator;
    private final OrderAuthorization authorization;

    @Transactional
    public CreateOrderResult handle(CreateOrderCommand command, UUID authenticatedCustomerId) {
        log.info("Creating new order for customer: {}", command.customerId());
        
        authorization.validateCreateOrderAuthorization(command.customerId(), authenticatedCustomerId);

        if (command.items() == null || command.items().isEmpty()) {
            throw new OrderException("Order must have at least one item");
        }

        Order order = mapper.toEntity(command);
        order.setCustomerId(command.customerId());

        for (OrderItemDto itemDto : command.items()) {
            validator.validateItem(itemDto);
            OrderItem item = mapper.toItemEntity(itemDto);
            item.calculateSubtotal();
            order.addItem(item);
        }

        order.calculateTotal();
        validator.validateOrderTotal(order.getTotal());

        Order saved = repository.save(order);
        log.info("Order created successfully with id: {}", saved.getId());
        
        return mapper.toResult(saved);
    }
}
