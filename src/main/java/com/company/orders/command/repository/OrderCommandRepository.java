package com.company.orders.command.repository;

import com.company.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderCommandRepository extends JpaRepository<Order, UUID> {
}
