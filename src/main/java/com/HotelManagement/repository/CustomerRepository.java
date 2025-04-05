package com.HotelManagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUserId(Integer userId);
    
    @Query("SELECT c FROM Customer c WHERE c.fullName LIKE %:query% OR c.phone LIKE %:query% OR c.email LIKE %:query%")
    List<Customer> searchByQuery(@Param("query") String query);
    
    @Query("SELECT c FROM Customer c WHERE c.idCard = :idCard")
    Optional<Customer> findByIdCard(@Param("idCard") String idCard);
    
    @Query("SELECT DISTINCT c FROM Customer c JOIN Booking b ON c.id = b.customer.id WHERE b.checkInDate >= :startDate")
    List<Customer> findCustomersWithRecentBookings(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT c FROM Customer c WHERE c.user IS NULL")
    List<Customer> findCustomersWithoutUserAccount();
    
    @Query("""
        SELECT c FROM Customer c 
        WHERE EXISTS (
            SELECT b FROM Booking b 
            WHERE b.customer = c 
            GROUP BY b.customer 
            HAVING COUNT(b) >= :bookingCount
        )
    """)
    List<Customer> findFrequentCustomers(@Param("bookingCount") Long bookingCount);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :startDate")
    Long countNewCustomers(@Param("startDate") LocalDateTime startDate);
}