package ent.orderManagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ent.orderManagement.model.Order;
import ent.orderManagement.model.OrderEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    
   Page<OrderEntity> findAll(Pageable pageable);

    // Fetch orders for a specific customer with pagination
    Page<OrderEntity> findByCustomerId(UUID customerId, Pageable pageable);

    // Check if an order exists by ID
    boolean existsById(UUID id);
}
