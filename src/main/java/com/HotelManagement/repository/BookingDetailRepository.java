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
    
    @Query("SELECT bd FROM BookingDetail bd WHERE bd.booking.status = :status")
    List<BookingDetail> findByBookingStatus(@Param("status") String status);
    
    @Query("SELECT bd FROM BookingDetail bd WHERE bd.room.id = :roomId AND bd.booking.checkInDate <= :date AND bd.booking.checkOutDate >= :date")
    List<BookingDetail> findByRoomIdAndDate(@Param("roomId") Integer roomId, @Param("date") LocalDateTime date);
    
    @Query("SELECT bd FROM BookingDetail bd WHERE bd.room.roomType.id = :roomTypeId")
    List<BookingDetail> findByRoomTypeId(@Param("roomTypeId") Integer roomTypeId);
    
    @Query("""
        SELECT bd FROM BookingDetail bd 
        WHERE bd.booking.checkInDate BETWEEN :startDate AND :endDate 
        OR bd.booking.checkOutDate BETWEEN :startDate AND :endDate
    """)
    List<BookingDetail> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(bd) FROM BookingDetail bd WHERE bd.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("""
        SELECT bd FROM BookingDetail bd 
        WHERE bd.room.id = :roomId 
        AND bd.booking.status IN ('CONFIRMED', 'CHECKED_IN')
        AND (
            (bd.booking.checkInDate BETWEEN :startDate AND :endDate)
            OR (bd.booking.checkOutDate BETWEEN :startDate AND :endDate)
            OR (bd.booking.checkInDate <= :startDate AND bd.booking.checkOutDate >= :endDate)
        )
    """)
    List<BookingDetail> findConflictingBookings(
        @Param("roomId") Integer roomId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}