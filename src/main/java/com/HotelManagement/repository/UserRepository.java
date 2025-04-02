package com.HotelManagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    
    User findByEmail(String email);
    
    User findByPhone(String phone);
    
    Optional<User> findByUsernameAndPassword(String username, String password);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.roleId = :roleId")
    long countByRoleId(int roleId);
}