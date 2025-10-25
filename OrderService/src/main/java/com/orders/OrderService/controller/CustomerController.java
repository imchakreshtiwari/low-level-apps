package com.orders.OrderService.controller;

import com.orders.OrderService.dto.CreateCustomerRequest;
import com.orders.OrderService.model.Customer;
import com.orders.OrderService.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerRepository customerRepository;
    public CustomerController(CustomerRepository customerRepository) { this.customerRepository = customerRepository; }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody @Valid CreateCustomerRequest request) {
        Customer c = new Customer();
        c.setName(request.getName());
        c.setEmail(request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(customerRepository.save(c));
    }

    @GetMapping
    public List<Customer> list() { return customerRepository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

