package com.HotelManagement.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.exception.BusinessException;
import com.HotelManagement.exception.ErrorCodes;
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
    
    /**
     * Hash a password using SHA-256
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ErrorCodes.USER_NOT_FOUND, "Error hashing password");
        }
    }
    
    /**
     * Verify a password against a hash
     */
    private boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
    
    public User login(String username, String password) 
    {
        User user = userRepository.findByUsername(username);
        if (user != null && verifyPassword(password, user.getPassword())) 
        {
            return user;
        }
        return null;
    }
    
    public User signup(String username, String password, String email, String fullName, String phone) 
    {
        // Check if user already exists
        if (userRepository.findByUsername(username) != null) 
        {
            throw new BusinessException(ErrorCodes.USERNAME_EXISTS);
        }
        
        if (userRepository.findByEmail(email) != null) 
        {
            throw new BusinessException(ErrorCodes.EMAIL_EXISTS);
        }
        
        if (userRepository.findByPhone(phone) != null) 
        {
            throw new BusinessException(ErrorCodes.PHONE_EXISTS);
        }
        
        // Create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(hashPassword(password)); // Hash password for security
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPhone(phone);
        newUser.setRoleId(0); // 0 for customer role
        
        // Save the user first to get the ID
        newUser = userRepository.save(newUser);
        
        // If this is a customer (roleId = 0), create a customer record
        if (newUser.getRoleId() == 0) {
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhone(phone);
            customer.setEmail(email);
            customer.setUser(newUser); // Link customer to user
            customerRepository.save(customer);
        }
        
        return newUser;
    }
}