package com.HotelManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByPhone(String phone);
    Customer findByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE c.fullName LIKE %:query% OR c.phone LIKE %:query% OR c.email LIKE %:query%")
    List<Customer> searchByQuery(@Param("query") String query);
}