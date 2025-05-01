package com.HotelManagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.HotelManagement.models.Customer;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    @Transactional
    public User saveUser(User user) {
        // Check if user already exists with the same username
        if (user.getId() == null) {
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser != null) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check for duplicate email and phone
            if (userRepository.findByEmail(user.getEmail()) != null) {
                throw new RuntimeException("Email already registered");
            }
            
            if (userRepository.findByPhone(user.getPhone()) != null) {
                throw new RuntimeException("Phone number already registered");
            }
        } else {
            // For updates, check if username exists but belongs to a different user
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("Username already exists");
            }
            
            // Similarly for email and phone
            User existingEmail = userRepository.findByEmail(user.getEmail());
            if (existingEmail != null && !existingEmail.getId().equals(user.getId())) {
                throw new RuntimeException("Email already registered");
            }
            
            User existingPhone = userRepository.findByPhone(user.getPhone());
            if (existingPhone != null && !existingPhone.getId().equals(user.getId())) {
                throw new RuntimeException("Phone number already registered");
            }
        }
        
        // Save the user first
        User savedUser = userRepository.save(user);
        
        // If user is a customer (roleId = 0), synchronize with customer data
        if (savedUser.getRoleId() != null && savedUser.getRoleId() == 0) {
            // Try to find an existing customer for this user
            Optional<Customer> existingCustomer = customerRepository.findByUserId(savedUser.getId());
            
            if (existingCustomer.isPresent()) {
                // Update existing customer data to match user data
                Customer customer = existingCustomer.get();
                customer.setFullName(savedUser.getFullName());
                customer.setEmail(savedUser.getEmail());
                customer.setPhone(savedUser.getPhone());
                customerRepository.save(customer);
            } else {
                // Create a new customer record if doesn't exist
                Customer newCustomer = new Customer();
                newCustomer.setUser(savedUser);
                newCustomer.setFullName(savedUser.getFullName());
                newCustomer.setEmail(savedUser.getEmail());
                newCustomer.setPhone(savedUser.getPhone());
                customerRepository.save(newCustomer);
            }
        }
        
        return savedUser;
    }
    
    @Transactional
    public void deleteUser(Integer id) {
        // Check if the user exists
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        // Check if this is not the last administrator
        User user = getUserById(id);
        if (user.getRoleId() == 1) {
            long adminCount = countUsersByRole(1);
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last administrator account");
            }
        }
        
        userRepository.deleteById(id);
    }
    
    public long countUsersByRole(int roleId) {
        return userRepository.countByRoleId(roleId);
    }
}