package com.HotelManagement.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    @Query("SELECT b FROM Booking b WHERE CAST(b.checkInDate AS date) = :date")
    List<Booking> findByCheckInDate(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b JOIN b.customer c WHERE c.phone = :phone")
    List<Booking> findByCustomerPhone(@Param("phone") String phone);
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") String status);
    
    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId")
    List<Booking> findByCustomerId(@Param("customerId") Integer customerId);
    
    @Query("SELECT b FROM Booking b WHERE b.checkInDate BETWEEN :startDate AND :endDate")
    List<Booking> findByCheckInDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND CAST(b.checkInDate AS date) = CAST(CURRENT_TIMESTAMP AS date)")
    List<Booking> findTodayCheckIns();
    
    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = :paymentStatus")
    List<Booking> findByPaymentStatus(@Param("paymentStatus") String paymentStatus);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND CAST(b.checkInDate AS date) = :date")
    Long countByStatusAndDate(@Param("status") String status, @Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.checkInDate > :date AND b.status = :status")
    List<Booking> findByCheckInDateAfterAndStatus(@Param("date") LocalDateTime date, @Param("status") String status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT b FROM Booking b WHERE b.status IN ('CONFIRMED', 'CHECKED_IN')")
    List<Booking> findAllActiveBookings();
}