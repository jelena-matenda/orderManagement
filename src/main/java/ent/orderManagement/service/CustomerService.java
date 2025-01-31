package ent.orderManagement.service;
import org.springframework.stereotype.Service;

import ent.orderManagement.model.Customer;
import ent.orderManagement.repository.CustomerRepository;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Constructor injection: Spring will provide the repository
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Create a new customer, applying any business logic checks if needed.
     */
    public Customer createCustomer(Customer customer) {
        // Example check: ensure the email isn't already in use, if desired
        // or ensure name has min length (though you'd often rely on Bean Validation)
        // For instance:
        // if (customerRepository.emailExists(customer.getEmail())) {
        //     throw new IllegalArgumentException("Email is already in use");
        // }

        return customerRepository.save(customer);
    }

    /**
     * Retrieve a single customer by UUID.
     */
    public Customer getCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    /**
     * Retrieve all customers.
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Update an existing customer with new data.
     * 
     * @param customerId the ID of the customer to update
     * @param newData    the new customer data (name, email, etc.)
     */
    public Customer updateCustomer(UUID customerId, Customer newData) {
        // Fetch existing customer
        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Update fields (only if they’re not null, or always override—depends on your logic)
        existing.setName(newData.getName());
        existing.setEmail(newData.getEmail());

        // Save changes
        return customerRepository.update(existing);
    }

    /**
     * Delete a customer by ID.
     */
    public void deleteCustomer(UUID customerId) {
        // Possibly check if customer has active orders, etc.
        // If you want to prevent deletion in certain scenarios, 
        // you can do additional checks here.

        customerRepository.deleteById(customerId);
    }

    /**
     * Check if a customer exists by ID (optional convenience method).
     */
    public boolean customerExists(UUID customerId) {
        return customerRepository.existsById(customerId);
    }
}

