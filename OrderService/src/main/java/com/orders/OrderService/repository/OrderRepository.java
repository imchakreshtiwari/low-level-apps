package com.orders.OrderService.repository;

import com.orders.OrderService.model.Order;
import com.orders.OrderService.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}

