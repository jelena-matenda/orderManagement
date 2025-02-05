package ent.orderManagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private Float totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.StatusEnum status;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdAt;

    // Constructors
    public OrderEntity() {}

    public OrderEntity(Order order) {
        this.id = order.getId() != null ? order.getId() : UUID.randomUUID();
        this.customerId = order.getCustomerId();
        this.orderDate = order.getOrderDate();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }

    // Convert Entity to DTO
    public Order toOrder() {
        return new Order()
                .id(this.id)
                .customerId(this.customerId)
                .orderDate(this.orderDate)
                .totalAmount(this.totalAmount)
                .status(this.status)
                .createdAt(this.createdAt);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public Float getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Float totalAmount) { this.totalAmount = totalAmount; }

    public Order.StatusEnum getStatus() { return status; }
    public void setStatus(Order.StatusEnum status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

