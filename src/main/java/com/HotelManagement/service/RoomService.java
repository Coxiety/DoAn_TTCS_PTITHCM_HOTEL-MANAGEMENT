package com.HotelManagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.models.Room;
import com.HotelManagement.repository.RoomRepository;

@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus("AVAILABLE");
    }
    
    public List<Room> getOccupiedRooms() {
        return roomRepository.findByStatus("OCCUPIED");
    }
    
    public List<Room> getBookedRooms() {
        return roomRepository.findByStatus("BOOKED");
    }
    
    public Room getRoomById(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }
    
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }
    
    public void updateRoomStatus(Integer roomId, String status) {
        Room room = getRoomById(roomId);
        room.setStatus(status);
        roomRepository.save(room);
    }
}