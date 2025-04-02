package com.HotelManagement.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByCheckInDateBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, String status);
    
    @Query("SELECT b FROM Booking b JOIN b.customer c WHERE c.phone = :phone AND b.status = :status")
    List<Booking> findByCustomerPhoneAndStatus(@Param("phone") String phone, @Param("status") String status);
}