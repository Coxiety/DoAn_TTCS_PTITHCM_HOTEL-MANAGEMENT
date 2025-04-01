package com.HotelManagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.models.Customer;
import com.HotelManagement.repository.CustomerRepository;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public Customer saveCustomer(Customer customer) {
        // Check if customer already exists with the same phone or email
        if (customer.getPhone() != null) {
            Customer existingPhone = customerRepository.findByPhone(customer.getPhone());
            if (existingPhone != null) {
                throw new RuntimeException("Customer with this phone already exists");
            }
        }
        
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            Customer existingEmail = customerRepository.findByEmail(customer.getEmail());
            if (existingEmail != null) {
                throw new RuntimeException("Customer with this email already exists");
            }
        }
        
        return customerRepository.save(customer);
    }
    
    public List<Customer> searchCustomers(String query) {
        return customerRepository.searchByQuery(query);
    }
    
    public Customer getCustomerById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}