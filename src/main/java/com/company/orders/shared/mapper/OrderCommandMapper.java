package com.company.orders.shared.mapper;

import com.company.orders.command.model.CreateOrderCommand;
import com.company.orders.command.model.CreateOrderResult;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.domain.entity.Order;
import com.company.orders.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(CreateOrderCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    OrderItem toItemEntity(OrderItemDto dto);

    @Mapping(target = "orderId", source = "id")
    CreateOrderResult toResult(Order entity);
}
