package com.orders.OrderService.model;

import jakarta.persistence.*;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = true)
    private String name;
    @Column(nullable = false)
    private String email;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}
