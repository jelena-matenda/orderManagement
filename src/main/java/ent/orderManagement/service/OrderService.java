package ent.orderManagement.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ent.orderManagement.model.Order;
import ent.orderManagement.model.Order.StatusEnum;
import ent.orderManagement.model.Role;
import ent.orderManagement.model.User;
import ent.orderManagement.repository.CustomerRepository;
import ent.orderManagement.repository.OrderRepository;
import ent.orderManagement.repository.UserRepository;

import java.util.List;
import java.util.UUID;

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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN || order.getCustomerId().equals(currentUser.getId())) {
            return order;
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
        return orderRepository.save(order);
    }

    /**
     * 📝 Update an order (Users can only update their own, Admins can update all).
     */
    public Order updateOrder(UUID orderId, Order newOrder) {
        Order existingOrder = getOrderById(orderId);
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
            return orderRepository.save(existingOrder);
        } else {
            throw new RuntimeException("Access denied: You can only update your own orders.");
        }
    }

    /**
     * 📝 Delete an order (Users can only delete their own, Admins can delete all).
     */
    public void deleteOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN || order.getCustomerId().equals(currentUser.getId())) {
            orderRepository.deleteById(orderId);
        } else {
            throw new RuntimeException("Access denied: You can only delete your own orders.");
        }
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
     * 📝 Get all orders (Admins can see all, Users only their own).
     */
    public List<Order> getOrders() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            return orderRepository.findAll(); // Admin sees all orders
        } else {
            return orderRepository.findByCustomerId(currentUser.getId()); // User sees only their orders
        }
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
