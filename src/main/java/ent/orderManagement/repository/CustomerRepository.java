package ent.orderManagement.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import ent.orderManagement.controller.CustomerController;
import ent.orderManagement.exception.DuplicateEmailException;
import ent.orderManagement.exception.DuplicateUuidException;
import ent.orderManagement.model.Customer;

@Repository
public class CustomerRepository {

    private static final Logger logger = LoggerFactory.getLogger(CustomerRepository.class);
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor Injection for JdbcTemplate.
     * Spring will automatically provide a configured JdbcTemplate
     * based on your application.yml datasource settings.
     */
    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * RowMapper that converts a ResultSet row into a Customer object.
     */
    private static final RowMapper<Customer> CUSTOMER_ROW_MAPPER = (rs, rowNum) -> {
        Customer customer = new Customer();
        customer.setId(UUID.fromString(rs.getString("id")));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            customer.setCreatedAt(createdAtTs.toLocalDateTime().atOffset(ZoneOffset.UTC));
        }

        return customer;
    };

    /**
     * Retrieve all customers from the 'customers' table.
     */
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers";
        return jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER);
    }

    /**
     * Retrieve a single Customer by ID.
     * @param id UUID of the customer
     * @return Optional containing the Customer if found, or empty if not found
     */
    public Optional<Customer> findById(UUID id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        logger.debug("Customer id: ", id);
        List<Customer> results = jdbcTemplate.query(sql, CUSTOMER_ROW_MAPPER, id);
        logger.debug("Results: ", results);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Create a new Customer record.
     * Uses 'RETURNING id' if supported by PostgreSQL driver 
     * to retrieve the inserted ID. 
     * @param customer The customer to save (name, email set; id may be null).
     * @return The saved Customer, with generated ID and createdAt set.
     */
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (id, name, email, created_at) "
                   + "VALUES (?, ?, ?, ?) RETURNING id";

        logger.debug("Customer : ", customer);


        // If the Customer doesn't have an ID yet, generate one
        UUID newId = (customer.getId() == null) ? UUID.randomUUID() : customer.getId();
        
        logger.debug("Customer id: ", newId);

        Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM customers WHERE id = ?", 
        Integer.class, 
        newId
        );

        if (count != null && count > 0) {
            throw new DuplicateUuidException("Duplicate UUID: Customer with ID " + newId + " already exists.");
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        try {
        UUID returnedId = jdbcTemplate.queryForObject(
        sql,
        (rs, rowNum) -> UUID.fromString(rs.getString("id")),  // Convert returned String to UUID
        newId,  // Pass UUID directly
        customer.getName(),
        customer.getEmail(),
        now
        );

        // Set the generated ID and timestamp
        customer.setId(returnedId);
        customer.setCreatedAt(now.toLocalDateTime().atOffset(ZoneOffset.UTC));
        return customer;
        } catch (DuplicateEmailException ex) {
        throw new DuplicateEmailException("Customer with the same Email already exists.");
        }
    }

    /**
     * Update an existing Customer record.
     * @param customer The updated Customer (must have a valid id)
     * @return The same customer object (assuming successful update)
     */
    public Customer update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(
            sql,
            customer.getName(),
            customer.getEmail(),
            customer.getId()
        );
        return customer;
    }

    /**
     * Delete a Customer by ID.
     */
    public void deleteById(UUID id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Check if a Customer with a given ID exists.
     * @return true if found, false otherwise
     */
    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM customers WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.toString());
        return (count != null && count > 0);
    }
}
