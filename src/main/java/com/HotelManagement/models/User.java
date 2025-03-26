package com.HotelManagement.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Users")
public class User 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100, unique = true)
    private String username;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(name = "full_name", length = 255)
    private String fullName;
    
    @Column(length = 12)
    private String phone;
    
    @Column(length = 255, unique = true)
    private String email;
    
    @Column(name = "role_id")
    private Integer roleId;
    
    @Column(length = 255)
    private String address;
}