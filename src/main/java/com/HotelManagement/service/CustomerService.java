package com.HotelManagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.HotelManagement.exception.BusinessException;
import com.HotelManagement.exception.ErrorCodes;
import com.HotelManagement.models.Customer;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.repository.UserRepository;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public Customer saveCustomer(Customer customer) {
        // Check if customer already exists with the same phone or email
        if (customer.getPhone() != null) {
            Optional<Customer> existingPhone = customerRepository.findByPhone(customer.getPhone());
            if (existingPhone.isPresent() && !existingPhone.get().getId().equals(customer.getId())) {
                throw new BusinessException(ErrorCodes.CUSTOMER_PHONE_EXISTS);
            }
        }
        
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            Optional<Customer> existingEmail = customerRepository.findByEmail(customer.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getId().equals(customer.getId())) {
                throw new BusinessException(ErrorCodes.CUSTOMER_EMAIL_EXISTS);
            }
        }
        
        // If the customer has a linked user, synchronize data
        if (customer.getUser() != null) {
            User user = customer.getUser();
            
            // Update the user's data to match the customer's data
            user.setFullName(customer.getFullName());
            user.setEmail(customer.getEmail());
            user.setPhone(customer.getPhone());
            
            // Save the user
            userRepository.save(user);
        }
        
        return customerRepository.save(customer);
    }
    
    public List<Customer> searchCustomers(String searchTerm) {
        return customerRepository.searchCustomers(searchTerm);
    }
    
    public Customer getCustomerById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.CUSTOMER_NOT_FOUND));
    }
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Customer getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCodes.CUSTOMER_NOT_FOUND));
    }
    
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCodes.CUSTOMER_NOT_FOUND));
    }
    
    public Customer getCustomerByUserId(Integer userId) {
        return customerRepository.findByUserId(userId)
<<<<<<< Updated upstream
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
    
    @Transactional
    public Customer linkCustomerToUser(Integer customerId, User user) {
        Customer customer = getCustomerById(customerId);
        customer.setUser(user);
        
        // Synchronize customer data with user data
        customer.setFullName(user.getFullName());
        customer.setEmail(user.getEmail());
        customer.setPhone(user.getPhone());
        
        return customerRepository.save(customer);
    }
    
    @Transactional
    public void deleteCustomer(Integer id) {
        Customer customer = getCustomerById(id);
        // You might want to add additional checks here
        // For example, check if the customer has any active bookings
        customerRepository.delete(customer);
=======
                .orElseThrow(() -> new BusinessException(ErrorCodes.CUSTOMER_NOT_FOUND));
>>>>>>> Stashed changes
    }
    
    @Transactional
    public Customer updateCustomer(Integer id, Customer customerDetails) {
        Customer customer = getCustomerById(id);
        
        // Update only non-null fields
        if (customerDetails.getFullName() != null) {
            customer.setFullName(customerDetails.getFullName());
        }
        if (customerDetails.getEmail() != null) {
            customer.setEmail(customerDetails.getEmail());
        }
        if (customerDetails.getPhone() != null) {
            customer.setPhone(customerDetails.getPhone());
        }
        
        // If the customer has a linked user, synchronize the user data as well
        if (customer.getUser() != null) {
            User user = customer.getUser();
            
            // Update the user data to match customer data
            user.setFullName(customer.getFullName());
            user.setEmail(customer.getEmail());
            user.setPhone(customer.getPhone());
            
            // Save the user
            userRepository.save(user);
        }
        
        return saveCustomer(customer);
    }
}