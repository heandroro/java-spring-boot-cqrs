package com.company.orders.mapper;

import com.company.orders.dto.CreateOrderRequest;
import com.company.orders.dto.OrderDto;
import com.company.orders.dto.OrderItemDto;
import com.company.orders.entity.Order;
import com.company.orders.entity.OrderItem;
import com.company.orders.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    OrderDto toDto(Order entity);

    OrderItemDto toItemDto(OrderItem entity);

    List<OrderDto> toDtoList(List<Order> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(CreateOrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    OrderItem toItemEntity(OrderItemDto dto);

    @Named("statusToString")
    default String statusToString(OrderStatus status) {
        return status != null ? status.getValue() : null;
    }
}
