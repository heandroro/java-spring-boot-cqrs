package com.company.orders.shared.mapper;

import com.company.orders.shared.model.OrderItemDto;
import com.company.orders.domain.entity.Order;
import com.company.orders.domain.entity.OrderItem;
import com.company.orders.domain.enums.OrderStatus;
import com.company.orders.query.model.OrderQueryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderQueryMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    OrderQueryResult toResult(Order entity);

    OrderItemDto toItemDto(OrderItem entity);

    List<OrderQueryResult> toResultList(List<Order> entities);

    @Named("statusToString")
    default String statusToString(OrderStatus status) {
        return status != null ? status.getValue() : null;
    }
}
