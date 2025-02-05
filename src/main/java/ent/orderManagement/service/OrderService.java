package ent.orderManagement.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ent.orderManagement.model.*;
import ent.orderManagement.model.Order.StatusEnum;
import ent.orderManagement.repository.CustomerRepository;
import ent.orderManagement.repository.OrderRepository;
import ent.orderManagement.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    /**
     * 📝 Get a single order by ID (Admins can see all, Users only their own).
     */
    public Order getOrderById(UUID orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN || orderEntity.getCustomerId().equals(currentUser.getId())) {
            return orderEntity.toOrder(); // Convert Entity -> DTO
        } else {
            throw new RuntimeException("Access denied: You can only view your own orders.");
        }
    }

    /**
     * 📝 Create an order (Users can only create orders for themselves).
     */
    public Order createOrder(Order order) {
        User currentUser = getCurrentUser();
        order.setCustomerId(currentUser.getId()); // Assign current user as customer

        OrderEntity orderEntity = new OrderEntity(order);
        OrderEntity savedOrder = orderRepository.save(orderEntity);
        return savedOrder.toOrder();  // Convert Entity -> DTO
    }

    /**
     * 📝 Update an order (Users can only update their own, Admins can update all).
     */
    public Order updateOrder(UUID orderId, Order newOrder) {
        OrderEntity existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN || existingOrder.getCustomerId().equals(currentUser.getId())) {
            existingOrder.setOrderDate(newOrder.getOrderDate());
            existingOrder.setTotalAmount(newOrder.getTotalAmount());

            // Validate status transition
            if (!canTransitionStatus(existingOrder.getStatus(), newOrder.getStatus())) {
                throw new IllegalStateException(
                    "Invalid status transition: " + existingOrder.getStatus() + " -> " + newOrder.getStatus()
                );
            }

            existingOrder.setStatus(newOrder.getStatus());
            OrderEntity updatedOrder = orderRepository.save(existingOrder);
            return updatedOrder.toOrder(); // Convert Entity -> DTO
        } else {
            throw new RuntimeException("Access denied: You can only update your own orders.");
        }
    }

    /**
     * 📝 Delete an order (Users can only delete their own, Admins can delete all).
     */
    public void deleteOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN || order.getCustomerId().equals(currentUser.getId())) {
            orderRepository.deleteById(orderId);
        } else {
            throw new RuntimeException("Access denied: You can only delete your own orders.");
        }
    }

    /**
     * 📝 Get all orders with pagination (Admins see all, Users see their own).
     */
    public Page<Order> getOrders(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<OrderEntity> orderPage;

        if (currentUser.getRole() == Role.ADMIN) {
            orderPage = orderRepository.findAll(pageable); // Admin sees all orders
        } else {
            orderPage = orderRepository.findByCustomerId(currentUser.getId(), pageable); // Users see only their orders
        }

        List<Order> orderList = orderPage.getContent().stream()
                .map(OrderEntity::toOrder) // Convert Entities -> DTOs
                .collect(Collectors.toList());

        return new PageImpl<>(orderList, pageable, orderPage.getTotalElements());
    }

    /**
     * 🛑 Get the currently authenticated user.
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
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
        return true;
    }
}
