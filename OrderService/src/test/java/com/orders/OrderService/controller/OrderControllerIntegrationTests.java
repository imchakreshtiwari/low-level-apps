package com.orders.OrderService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orders.OrderService.dto.CreateOrderRequest;
import com.orders.OrderService.model.Customer;
import com.orders.OrderService.model.Product;
import com.orders.OrderService.model.OrderStatus;
import com.orders.OrderService.repository.CustomerRepository;
import com.orders.OrderService.repository.ProductRepository;
import com.orders.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    private Long customerId;
    private Long productId;
    @BeforeEach
    void init() {
        // Ensure a clean state before each test to avoid unique email constraint violations
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
        Customer c = new Customer();
        c.setName("John");
        c.setEmail("john@example.com");
        customerId = customerRepository.save(c).getId();
        Product p = new Product();
        p.setName("Widget");
        p.setPrice(5.5);
        productId = productRepository.save(p).getId();
    }
    private String createSampleOrder(int qty) throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(customerId);
        CreateOrderRequest.OrderLine line = new CreateOrderRequest.OrderLine();
        line.setProductId(productId);
        line.setQuantity(qty);
        req.setItems(List.of(line));
        String json = objectMapper.writeValueAsString(req);
        return mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()))
                .andExpect(jsonPath("$.total").value(qty * 5.5))
                .andReturn().getResponse().getContentAsString();
    }
    @Test
    void createFetchAndListOrder() throws Exception {
        createSampleOrder(3);
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customer.name").value("John"))
                .andExpect(jsonPath("$[0].items[0].productName").value("Widget"));
    }
    @Test
    void updateStatusAndCancelRules() throws Exception {
        String createdJson = createSampleOrder(3);
        Long id = objectMapper.readTree(createdJson).get("id").asLong();
        mockMvc.perform(put("/api/orders/" + id + "/status").param("status", OrderStatus.PROCESSING.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.PROCESSING.name()));
        mockMvc.perform(post("/api/orders/" + id + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    @Test
    void cancelPendingOrder() throws Exception {
        String createdJson = createSampleOrder(3);
        Long id = objectMapper.readTree(createdJson).get("id").asLong();
        mockMvc.perform(post("/api/orders/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.CANCELLED.name()));
    }
}
