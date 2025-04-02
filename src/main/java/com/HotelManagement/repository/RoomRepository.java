package com.HotelManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.HotelManagement.models.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByStatus(String status);
    
    Room findByRoomNumber(String roomNumber);
    
    // Method to find rooms by type ID and status
    List<Room> findByRoomTypeIdAndStatus(Integer typeId, String status);
}