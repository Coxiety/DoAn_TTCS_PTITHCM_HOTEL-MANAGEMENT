package com.HotelManagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.RoomRepository;
import com.HotelManagement.repository.RoomTypeRepository;

import jakarta.transaction.Transactional;

@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    // ================== Room Methods ==================
    
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus("AVAILABLE");
    }
    
    public List<Room> getOccupiedRooms() {
        return roomRepository.findByStatus("OCCUPIED");
    }
    
    public List<Room> getAvailableRoomsByType(Integer typeId) {
        return roomRepository.findByRoomTypeIdAndStatus(typeId, "AVAILABLE");
    }
    
    public Room getRoomById(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }
    
    public Room getRoomByRoomNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }
    
    public Room saveRoom(Room room) {
        // Check if the room number is unique
        if (room.getId() == null) {
            Room existingRoom = roomRepository.findByRoomNumber(room.getRoomNumber());
            if (existingRoom != null) {
                throw new RuntimeException("Room number already exists");
            }
        }
        
        return roomRepository.save(room);
    }
    
    @Transactional
    public void deleteRoom(Integer id) {
        // Check if the room exists
        Room room = getRoomById(id);
        
        // Check if room has active bookings
        boolean hasActiveBookings = bookingDetailRepository.findAll().stream()
                .filter(bd -> bd.getRoom().getId().equals(id))
                .anyMatch(bd -> bd.getStatus().equals("CONFIRMED") || bd.getStatus().equals("OCCUPIED"));
        
        if (hasActiveBookings) {
            throw new RuntimeException("Cannot delete room with active bookings");
        }
        
        roomRepository.deleteById(id);
    }
    
    /**
     * Check if a room is currently in use (has active bookings).
     * 
     * @param roomId the ID of the room to check
     * @return true if the room is in use, false otherwise
     */
    public boolean isRoomInUse(Integer roomId) {
        // Check if the room has active bookings
        return bookingDetailRepository.findAll().stream()
                .filter(bd -> bd.getRoom().getId().equals(roomId))
                .anyMatch(bd -> bd.getStatus().equals("CONFIRMED") || bd.getStatus().equals("OCCUPIED"));
    }
    
    // ================== Room Type Methods ==================
    
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }
    
    public RoomType getRoomTypeById(Integer id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + id));
    }
    
    public RoomType saveRoomType(RoomType roomType) {
        // Check if the room type name is unique
        if (roomType.getId() == null) {
            RoomType existingType = roomTypeRepository.findByName(roomType.getName());
            if (existingType != null) {
                throw new RuntimeException("Room type name already exists");
            }
        }
        
        return roomTypeRepository.save(roomType);
    }
    
    @Transactional
    public void deleteRoomType(Integer id) {
        // Check if the room type exists
        RoomType roomType = getRoomTypeById(id);
        
        // Check if there are rooms associated with this type
        boolean hasRooms = roomRepository.findAll().stream()
                .anyMatch(room -> room.getRoomType().getId().equals(id));
        
        if (hasRooms) {
            throw new RuntimeException("Cannot delete room type that has rooms associated with it");
        }
        
        roomTypeRepository.deleteById(id);
    }
    
    /**
     * Check if a room type is in use (has rooms assigned to it).
     * 
     * @param roomTypeId the ID of the room type to check
     * @return true if the room type is in use, false otherwise
     */
    public boolean isRoomTypeInUse(Integer roomTypeId) {
        return roomRepository.findAll().stream()
                .anyMatch(room -> room.getRoomType().getId().equals(roomTypeId));
    }
}