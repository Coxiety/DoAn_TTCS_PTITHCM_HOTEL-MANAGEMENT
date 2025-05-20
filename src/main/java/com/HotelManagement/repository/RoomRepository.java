package com.HotelManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByStatus(String status);
    
    Room findByRoomNumber(String roomNumber);
    
    // Method to find rooms by type ID and status
    List<Room> findByRoomTypeIdAndStatus(Integer typeId, String status);
    
    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.roomType.id = :typeId")
    List<Room> findAvailableRoomsByType(@Param("typeId") Integer typeId);
    
    @Query("""
        SELECT r FROM Room r 
        WHERE r.id NOT IN (
            SELECT bd.room.id FROM BookingDetail bd 
            WHERE bd.booking.status IN ('CONFIRMED', 'CHECKED_IN')
        )
        AND r.status = 'AVAILABLE'
        AND r.roomType.id = :typeId
    """)
    List<Room> findAvailableRoomsForBooking(@Param("typeId") Integer typeId);
}