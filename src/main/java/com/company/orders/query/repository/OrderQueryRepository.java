package com.company.orders.query.repository;

import com.company.orders.entity.Order;
import com.company.orders.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface OrderQueryRepository extends Repository<Order, UUID> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findAll(Pageable pageable);

    long count();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId AND o.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") OrderStatus status);
}
