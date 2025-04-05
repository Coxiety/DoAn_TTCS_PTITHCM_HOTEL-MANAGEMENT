package com.HotelManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.models.Customer;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.CustomerRepository;
import com.HotelManagement.repository.UserRepository;

@Service
public class AuthService 
{
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public User login(String username, String password) 
    {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) 
        {
            // Note: Should use password hashing in production
            return user;
        }
        return null;
    }
    
    public User signup(String username, String password, String email, String fullName, String phone) 
    {
        // Check if user already exists
        if (userRepository.findByUsername(username) != null) 
        {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.findByEmail(email) != null) 
        {
            throw new RuntimeException("Email already exists");
        }
        
        if (userRepository.findByPhone(phone) != null) 
        {
            throw new RuntimeException("Phone number already exists");
        }
        
        // Create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // Note: Should hash password in production
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
        newUser.setRoleId(2); // 2 for customer role
        
        // Save the user first to get the ID
        newUser = userRepository.save(newUser);
        
        // If this is a customer (roleId = 2), create a customer record
        if (newUser.getRoleId() == 2) {
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhone(phone);
            customer.setEmail(email);
            customerRepository.save(customer);
        }
        
        return newUser;
    }
}