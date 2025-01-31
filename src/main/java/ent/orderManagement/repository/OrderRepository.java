package ent.orderManagement.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ent.orderManagement.model.Order;
import ent.orderManagement.model.Order.StatusEnum;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Maps a row in the 'orders' table to an Order object
    private static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, rowNum) -> {
        Order order = new Order();
        order.setId(UUID.fromString(rs.getString("id")));
        order.setCustomerId(UUID.fromString(rs.getString("customer_id")));
        order.setOrderDate(rs.getDate("order_date").toLocalDate());
        order.setTotalAmount(rs.getFloat("total_amount"));

        // Convert status from String to Enum
        order.setStatus(StatusEnum.valueOf(rs.getString("status")));

        // created_at is a TIMESTAMP in DB, mapped to LocalDateTime
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            order.setCreatedAt(createdTs.toLocalDateTime().atOffset(ZoneOffset.UTC));
        }

        return order;
    };

    /**
     * Find all orders in the 'orders' table.
     */
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";
        return jdbcTemplate.query(sql, ORDER_ROW_MAPPER);
    }

    /**
     * Find an order by its UUID.
     */
    public Optional<Order> findById(UUID id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        List<Order> list = jdbcTemplate.query(sql, ORDER_ROW_MAPPER, id.toString());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Save (insert) a new Order into the table.
     * Using PostgreSQL's RETURNING feature to get the inserted id back.
     */
    public Order save(Order order) {
        String sql = """
            INSERT INTO orders
            (id, customer_id, order_date, total_amount, status, created_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            RETURNING id, created_at
            """;
        
        // Generate a new ID if not already set
        UUID newId = (order.getId() == null) ? UUID.randomUUID() : order.getId();

        // We can read back both 'id' and 'created_at' from RETURNING
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            order.setId(UUID.fromString(rs.getString("id")));
            order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().atOffset(ZoneOffset.UTC));
            return order; 
        },
        newId.toString(),
        order.getCustomerId().toString(),
        order.getOrderDate(),
        order.getTotalAmount(),
        order.getStatus().toString()
        );
    }

    /**
     * Update an existing Order.
     */
    public Order update(Order order) {
        String sql = """
            UPDATE orders
               SET customer_id = ?, 
                   order_date = ?, 
                   total_amount = ?, 
                   status = ?
             WHERE id = ?
        """;
        jdbcTemplate.update(sql,
            order.getCustomerId().toString(),
            order.getOrderDate(),
            order.getTotalAmount(),
            order.getStatus().toString(),
            order.getId().toString()
        );
        return order;
    }

    /**
     * Delete an order by ID.
     */
    public void deleteById(UUID id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        jdbcTemplate.update(sql, id.toString());
    }

    /**
     * Check if an order with this ID exists.
     */
    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM orders WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.toString());
        return (count != null && count > 0);
    }
}
