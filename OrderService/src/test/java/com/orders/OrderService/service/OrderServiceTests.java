package com.orders.OrderService.service;

import com.orders.OrderService.dto.CreateOrderRequest;
import com.orders.OrderService.dto.OrderResponse;
import com.orders.OrderService.exception.InvalidOrderStateException;
import com.orders.OrderService.model.*;
import com.orders.OrderService.repository.CustomerRepository;
import com.orders.OrderService.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
@Import(OrderServiceImpl.class)
class OrderServiceTests {
    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    private Long customerId;
    private Long productId;
    @BeforeEach
    void setup() {
        Customer c = new Customer();
        c.setName("John Doe");
        c.setEmail("john@example.com");
        customerId = customerRepository.save(c).getId();
        Product p = new Product();
        p.setName("Widget");
        p.setPrice(9.99);
        productId = productRepository.save(p).getId();
    }
    private CreateOrderRequest sampleRequest(int qty) {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(customerId);
        CreateOrderRequest.OrderLine line = new CreateOrderRequest.OrderLine();
        line.setProductId(productId);
        line.setQuantity(qty);
        req.setItems(List.of(line));
        return req;
    }
    @Test
    void createAndFetchOrder() {
        OrderResponse created = orderService.createOrder(sampleRequest(2));
        Assertions.assertEquals(19.98, created.getTotal(), 0.0001);
        OrderResponse fetched = orderService.getOrder(created.getId());
        Assertions.assertEquals(created.getId(), fetched.getId());
        Assertions.assertEquals(OrderStatus.PENDING, fetched.getStatus());
    }
    @Test
    void listOrdersByStatus() {
        orderService.createOrder(sampleRequest(2));
        orderService.createOrder(sampleRequest(3));
        int promoted = orderService.promotePendingToProcessing();
        Assertions.assertEquals(2, promoted);
        List<OrderResponse> processing = orderService.listOrders(OrderStatus.PROCESSING);
        Assertions.assertEquals(2, processing.size());
    }
    @Test
    void cancelPendingOrder() {
        OrderResponse created = orderService.createOrder(sampleRequest(2));
        OrderResponse cancelled = orderService.cancelOrder(created.getId());
        Assertions.assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }
    @Test
    void cannotCancelNonPendingOrder() {
        OrderResponse created = orderService.createOrder(sampleRequest(2));
        orderService.updateStatus(created.getId(), OrderStatus.PROCESSING);
        Assertions.assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(created.getId()));
    }
}
