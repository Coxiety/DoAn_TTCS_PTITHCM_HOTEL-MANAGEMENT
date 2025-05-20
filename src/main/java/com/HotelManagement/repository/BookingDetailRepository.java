package com.HotelManagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.BookingDetail;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {
    List<BookingDetail> findByBookingId(Integer bookingId);
    Optional<BookingDetail> findByRoomIdAndStatus(Integer roomId, String status);
    
    @Query("SELECT bd FROM BookingDetail bd WHERE bd.booking.id IN :bookingIds")
    List<BookingDetail> findByBookingIdIn(@Param("bookingIds") List<Integer> bookingIds);
    
    @Query("""
        SELECT bd FROM BookingDetail bd 
        WHERE bd.room.id = :roomId 
        AND bd.booking.status IN ('CONFIRMED', 'CHECKED_IN') 
        AND (
            (bd.booking.checkInDate <= :endDate AND bd.booking.checkOutDate >= :startDate)
            OR (bd.booking.checkInDate BETWEEN :startDate AND :endDate)
            OR (bd.booking.checkOutDate BETWEEN :startDate AND :endDate)
        )
    """)
    List<BookingDetail> findConflictingBookings(
        @Param("roomId") Integer roomId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}