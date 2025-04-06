package com.HotelManagement.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.HotelManagement.models.User;
import com.HotelManagement.repository.UserRepository;

/**
 * Utility to reset all user passwords to "test".
 * Run with VM argument: -Dspring.profiles.active=pwd-reset
 */
@Component
@Profile("pwd-reset")
public class PasswordResetUtil implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("Starting password reset utility...");
        
        // Get all users
        List<User> users = userRepository.findAll();
        System.out.println("Found " + users.size() + " users.");
        
        // The password to set for all users
        String newPassword = "test";
        String hashedPassword = hashPassword(newPassword);
        
        System.out.println("New password will be: " + newPassword);
        System.out.println("Hashed value: " + hashedPassword);
        
        // Update all users
        for (User user : users) {
            String oldUsername = user.getUsername();
            user.setPassword(hashedPassword);
            userRepository.save(user);
            System.out.println("Updated password for user: " + oldUsername);
        }
        
        System.out.println("Password reset completed successfully!");
    }
    
    /**
     * Hash a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
} 