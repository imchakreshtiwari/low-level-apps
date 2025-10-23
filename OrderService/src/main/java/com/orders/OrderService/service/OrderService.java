package com.orders.OrderService.service;

import com.orders.OrderService.dto.CreateOrderRequest;
import com.orders.OrderService.dto.OrderResponse;
import com.orders.OrderService.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(Long id);
    List<OrderResponse> listOrders(OrderStatus status);
    OrderResponse updateStatus(Long id, OrderStatus newStatus);
    OrderResponse cancelOrder(Long id);
    int promotePendingToProcessing();
}
