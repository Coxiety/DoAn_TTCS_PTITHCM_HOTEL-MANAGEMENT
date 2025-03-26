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
@Table(name = "Customers")
public class Customer 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;
    
    @Column(nullable = false, length = 12, unique = true)
    private String phone;
    
    @Column(length = 255, unique = true)
    private String email;
    
    @Column(length = 255)
    private String address;
    
    @Column(name = "booking_history", columnDefinition = "text")
    private String bookingHistory;
}