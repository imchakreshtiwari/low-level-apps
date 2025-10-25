package com.orders.OrderService.controller;

import com.orders.OrderService.dto.CreateProductRequest;
import com.orders.OrderService.model.Product;
import com.orders.OrderService.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    public ProductController(ProductRepository productRepository) { this.productRepository = productRepository; }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid CreateProductRequest request) {
        Product p = new Product();
        p.setName(request.getName());
        p.setPrice(request.getPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(p));
    }

    @GetMapping
    public List<Product> list() { return productRepository.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
