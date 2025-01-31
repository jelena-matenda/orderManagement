package ent.orderManagement.service;

import org.springframework.stereotype.Service;

import ent.orderManagement.model.Order;
import ent.orderManagement.model.Order.StatusEnum;
import ent.orderManagement.repository.CustomerRepository;
import ent.orderManagement.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Create a new order with business logic checks:
     * 1) Ensure the referenced customerId exists.
     * 2) totalAmount must be > 0.
     * 3) If status is not provided, default to NEW.
     */
    public Order createOrder(Order order) {
        if (order.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        // Check if customer exists
        if (!customerRepository.existsById(order.getCustomerId())) {
            throw new RuntimeException("Customer not found: " + order.getCustomerId());
        }

        // Ensure totalAmount > 0
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO.floatValue()) <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than 0");
        }

        // If orderDate is not provided, you could set it to "today" or throw an error.
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        // If status is null, default to NEW
        if (order.getStatus() == null) {
            order.setStatus(StatusEnum.NEW);
        }

        return orderRepository.save(order);
    }

    /**
     * Retrieve an Order by its ID (throws an exception if not found).
     */
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    /**
     * Retrieve all orders from the database.
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Update an existing order. 
     * Only allow status changes from NEW -> IN_PROGRESS or COMPLETED (example rule).
     */
    public Order updateOrder(UUID orderId, Order newData) {
        // Fetch existing order
        Order existing = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate status transition
        if (!canTransitionStatus(existing.getStatus(), newData.getStatus())) {
            throw new IllegalStateException(
                "Invalid status transition: " + existing.getStatus() + " -> " + newData.getStatus()
            );
        }

        // If needed, ensure new totalAmount is > 0
        if (newData.getTotalAmount() != null && newData.getTotalAmount().compareTo(BigDecimal.ZERO.floatValue()) <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than 0");
        }

        // Update fields (choose which ones to allow overriding)
        existing.setOrderDate(newData.getOrderDate() != null ? newData.getOrderDate() : existing.getOrderDate());
        existing.setTotalAmount(newData.getTotalAmount() != null ? newData.getTotalAmount() : existing.getTotalAmount());
        existing.setStatus(newData.getStatus() != null ? newData.getStatus() : existing.getStatus());

        return orderRepository.update(existing);
    }

    /**
     * Delete an Order by ID. 
     */
    public void deleteOrder(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        // Optionally add checks (e.g., if order is in certain status, can't delete).
        orderRepository.deleteById(orderId);
    }

    /**
     * Example logic for allowed status transitions:
     *  - If the old status is NEW, we can go to IN_PROGRESS or COMPLETED.
     *  - Otherwise, no restriction (you can refine logic as needed).
     */
    private boolean canTransitionStatus(StatusEnum oldStatus, StatusEnum newStatus) {
        if (oldStatus == StatusEnum.NEW) {
            return (newStatus == StatusEnum.IN_PROGRESS || newStatus == StatusEnum.COMPLETED);
        }
        // If we want to allow updates from IN_PROGRESS -> COMPLETED, etc., we can do that here
        return true;
    }
}
