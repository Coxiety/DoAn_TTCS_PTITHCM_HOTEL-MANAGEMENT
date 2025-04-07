package com.HotelManagement.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId")
    List<Payment> findByBookingId(@Param("bookingId") Integer bookingId);
    
    @Query("SELECT p FROM Payment p WHERE DATE(p.paymentDate) = :date")
    List<Payment> findByPaymentDate(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPaymentDateBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findByStatus(@Param("status") String status);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :method")
    List<Payment> findByPaymentMethod(@Param("method") String method);
} 