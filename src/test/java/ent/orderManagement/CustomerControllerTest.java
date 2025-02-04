package ent.orderManagement;

import ent.orderManagement.controller.CustomerController;
import ent.orderManagement.model.Customer;
import ent.orderManagement.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private Customer testCustomer;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
        testCustomerId = UUID.randomUUID();
        testCustomer = new Customer();
        testCustomer.setId(testCustomerId);
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john.doe@example.com");
    }

    @Test
    void shouldReturnAllCustomers() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(Collections.singletonList(testCustomer));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testCustomerId.toString()))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void shouldReturnCustomerById() throws Exception {
        when(customerService.getCustomer(testCustomerId)).thenReturn(testCustomer);

        mockMvc.perform(get("/customers/{id}", testCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCustomerId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).getCustomer(testCustomerId);
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        when(customerService.createCustomer(any(Customer.class))).thenReturn(testCustomer);

        String requestBody = """
            {
                "name": "John Doe",
                "email": "john.doe@example.com"
            }
            """;

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).createCustomer(any(Customer.class));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        when(customerService.updateCustomer(eq(testCustomerId), any(Customer.class))).thenReturn(testCustomer);

        String requestBody = """
            {
                "name": "Updated Name",
                "email": "updated.email@example.com"
            }
            """;

        mockMvc.perform(put("/customers/{id}", testCustomerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))  // Because service mock still returns old name
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).updateCustomer(eq(testCustomerId), any(Customer.class));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        doNothing().when(customerService).deleteCustomer(testCustomerId);

        mockMvc.perform(delete("/customers/{id}", testCustomerId))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(testCustomerId);
    }
}
