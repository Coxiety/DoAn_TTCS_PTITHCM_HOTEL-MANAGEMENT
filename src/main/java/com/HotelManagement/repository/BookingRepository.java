package com.HotelManagement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    @Query("SELECT b FROM Booking b WHERE DATE(b.checkInDate) = :date")
    List<Booking> findByCheckInDate(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b JOIN b.customer c WHERE c.phone = :phone")
    List<Booking> findByCustomerPhone(@Param("phone") String phone);
}