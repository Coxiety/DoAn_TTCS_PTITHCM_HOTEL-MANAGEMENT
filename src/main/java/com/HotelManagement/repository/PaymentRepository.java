package com.HotelManagement.repository;

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
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPaymentDateBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
} 