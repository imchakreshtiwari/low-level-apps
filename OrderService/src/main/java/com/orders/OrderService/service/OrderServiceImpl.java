package com.orders.OrderService.service;

import com.orders.OrderService.dto.CreateOrderRequest;
import com.orders.OrderService.dto.OrderItemDto;
import com.orders.OrderService.dto.OrderResponse;
import com.orders.OrderService.exception.InvalidOrderStateException;
import com.orders.OrderService.exception.OrderNotFoundException;
import com.orders.OrderService.model.*;
import com.orders.OrderService.repository.CustomerRepository;
import com.orders.OrderService.repository.OrderRepository;
import com.orders.OrderService.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new InvalidOrderStateException("Customer not found: " + request.getCustomerId()));
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        request.getItems().forEach(line -> {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new InvalidOrderStateException("Product not found: " + line.getProductId()));
            OrderItem oi = new OrderItem();
            oi.setProduct(product);
            oi.setQuantity(line.getQuantity());
            order.addItem(oi);
        });
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> listOrders(OrderStatus status) {
        List<Order> orders = status == null ? orderRepository.findAll() : orderRepository.findByStatus(status);
        return orders.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Cannot change status of a cancelled order");
        }
        order.setStatus(newStatus);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(order);
    }

    @Override
    @Transactional
    public int promotePendingToProcessing() {
        List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING);
        pending.forEach(o -> o.setStatus(OrderStatus.PROCESSING));
        return pending.size();
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        OrderResponse.CustomerDto cd = new OrderResponse.CustomerDto();
        cd.setId(order.getCustomer().getId());
        cd.setName(order.getCustomer().getName());
        cd.setEmail(order.getCustomer().getEmail());
        response.setCustomer(cd);
        List<OrderItemDto> items = order.getItems().stream().map(oi -> {
            OrderItemDto dto = new OrderItemDto();
            dto.setId(oi.getId());
            dto.setProductId(oi.getProduct().getId());
            dto.setProductName(oi.getProduct().getName());
            dto.setUnitPrice(oi.getProduct().getPrice());
            dto.setQuantity(oi.getQuantity());
            dto.setLineTotal(oi.getProduct().getPrice() * oi.getQuantity());
            return dto;
        }).collect(Collectors.toList());
        response.setItems(items);
        response.setTotal(items.stream().mapToDouble(OrderItemDto::getLineTotal).sum());
        return response;
    }
}
