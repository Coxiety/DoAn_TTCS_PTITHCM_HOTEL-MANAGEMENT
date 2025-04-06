package com.HotelManagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.models.User;
import com.HotelManagement.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
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
        
        // Here we would typically encrypt the password before saving
        // passwordEncoder.encode(user.getPassword())
        
        return userRepository.save(user);
    }
    
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