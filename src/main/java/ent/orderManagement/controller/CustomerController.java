package ent.orderManagement.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ent.orderManagement.model.Customer;
import ent.orderManagement.service.CustomerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // GET /customers
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // GET /customers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable UUID id) {
        Customer c = customerService.getCustomer(id);
        return ResponseEntity.ok(c);
    }

    // POST /customers
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer saved = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /customers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
        @PathVariable UUID id,
        @Valid @RequestBody Customer newData
    ) {
        Customer updated = customerService.updateCustomer(id, newData);
        return ResponseEntity.ok(updated);
    }

    // DELETE /customers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}

