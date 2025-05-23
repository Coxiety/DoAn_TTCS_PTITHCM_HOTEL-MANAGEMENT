package com.HotelManagement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.HotelManagement.exception.BusinessException;
import com.HotelManagement.exception.ErrorCodes;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.repository.RoomRepository;
import com.HotelManagement.repository.RoomTypeRepository;

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
        if (typeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }
        return roomRepository.findByRoomTypeIdAndStatus(typeId, "AVAILABLE");
    }
    
    public Room getRoomById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        return roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.ROOM_NOT_FOUND));
    }
    
    public Room getRoomByRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be empty");
        }
        Room room = roomRepository.findByRoomNumber(roomNumber);
        if (room == null) {
            throw new BusinessException(ErrorCodes.ROOM_NOT_FOUND_BY_NUMBER, "Room not found with number: " + roomNumber);
        }
        return room;
    }
    
    @Transactional
    public Room saveRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        
        // Validate room number
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Room number cannot be empty");
        }
        
        // Validate room type
        if (room.getRoomType() == null || room.getRoomType().getId() == null) {
            throw new IllegalArgumentException("Room type must be specified");
        }
        
        // Check if the room type exists
        getRoomTypeById(room.getRoomType().getId());
        
        // Check if the room number is unique
        if (room.getId() == null) {
            Room existingRoom = roomRepository.findByRoomNumber(room.getRoomNumber());
            if (existingRoom != null) {
                throw new BusinessException(ErrorCodes.ROOM_NUMBER_EXISTS, "Room number already exists: " + room.getRoomNumber());
            }
        } else {
            // For updates, check if the room number is unique among other rooms
            Room existingRoom = roomRepository.findByRoomNumber(room.getRoomNumber());
            if (existingRoom != null && !existingRoom.getId().equals(room.getId())) {
                throw new BusinessException(ErrorCodes.ROOM_NUMBER_EXISTS, "Room number already exists: " + room.getRoomNumber());
            }
        }
        
        // Set default status if not provided
        if (room.getStatus() == null || room.getStatus().trim().isEmpty()) {
            room.setStatus("AVAILABLE");
        }
        
        return roomRepository.save(room);
    }
    
    @Transactional
    public void deleteRoom(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        
        // Check if the room exists
        getRoomById(id);
        
        // Check if room has active bookings
        boolean hasActiveBookings = bookingDetailRepository.findAll().stream()
                .filter(bd -> bd.getRoom().getId().equals(id))
                .anyMatch(bd -> bd.getStatus().equals("CONFIRMED") || bd.getStatus().equals("OCCUPIED"));
        
        if (hasActiveBookings) {
            throw new BusinessException(ErrorCodes.ROOM_HAS_BOOKINGS);
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
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        
        // Check if the room exists
        getRoomById(roomId);
        
        // Check if the room has active bookings
        return bookingDetailRepository.findAll().stream()
                .filter(bd -> bd.getRoom().getId().equals(roomId))
                .anyMatch(bd -> bd.getStatus().equals("CONFIRMED") || bd.getStatus().equals("OCCUPIED"));
    }
    
    /**
     * Update the status of a room.
     * 
     * @param roomId the ID of the room to update
     * @param newStatus the new status to set
     * @return the updated room
     */
    @Transactional
    public Room updateRoomStatus(Integer roomId, String newStatus) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        
        // Validate status
        if (!isValidRoomStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid room status: " + newStatus);
        }
        
        // Get the room
        Room room = getRoomById(roomId);
        
        // Update the status
        room.setStatus(newStatus);
        
        return roomRepository.save(room);
    }
    
    /**
     * Check if a room is available for a specific date range.
     * 
     * @param roomId the ID of the room to check
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return true if the room is available, false otherwise
     */
    public boolean isRoomAvailableForDateRange(Integer roomId, LocalDateTime startDate, LocalDateTime endDate) {
        if (roomId == null) {
            throw new IllegalArgumentException("Room ID cannot be null");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Check if the room exists
        getRoomById(roomId);
        
        // Check for conflicting bookings
        List<BookingDetail> conflictingBookings = bookingDetailRepository.findConflictingBookings(roomId, startDate, endDate);
        
        return conflictingBookings.isEmpty();
    }
    
    /**
     * Check if a room status is valid.
     * 
     * @param status the status to check
     * @return true if the status is valid, false otherwise
     */
    private boolean isValidRoomStatus(String status) {
        return status != null && (
            status.equals("AVAILABLE") ||
            status.equals("OCCUPIED") ||
            status.equals("MAINTENANCE") ||
            status.equals("CLEANING") ||
            status.equals("BOOKED")
        );
    }
    
    // ================== Room Type Methods ==================
    
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepository.findAll();
    }
    
    public RoomType getRoomTypeById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCodes.ROOM_TYPE_NOT_FOUND));
    }
    
    @Transactional
    public RoomType saveRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null");
        }
        
<<<<<<< Updated upstream
        // Validate name uniqueness
        if (roomType.getId() == null) {
            RoomType existingType = roomTypeRepository.findByName(roomType.getName());
            if (existingType != null) {
                throw new RuntimeException("Trường bắt buộc và phải là duy nhất");
            }
        } else {
            // For updates, check if the name is unique among other room types
            RoomType existingType = roomTypeRepository.findByName(roomType.getName());
            if (existingType != null && !existingType.getId().equals(roomType.getId())) {
                throw new RuntimeException("Trường bắt buộc và phải là duy nhất");
            }
        }
        
        // Validate name
        if (roomType.getName() == null || roomType.getName().trim().isEmpty()) {
            throw new RuntimeException("Trường bắt buộc và phải là duy nhất");
        }
        
        // Validate capacity
        if (roomType.getCapacity() == null || roomType.getCapacity() <= 0) {
            throw new RuntimeException("Giá trị phải lớn hơn 0");
=======
        // Validate name
        if (roomType.getName() == null || roomType.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Room type name cannot be empty");
        }
        
        // Validate capacity
        if (roomType.getCapacity() == null || roomType.getCapacity() < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
>>>>>>> Stashed changes
        }
        
        // Validate price
        if (roomType.getPrice() == null || roomType.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
<<<<<<< Updated upstream
            throw new RuntimeException("Giá trị phải lớn hơn 0");
        }
        
        // Validate description
        if (roomType.getDescription() == null || roomType.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Trường bắt buộc, không được để trống");
        }
        
        // Validate amenities
        if (roomType.getAmenities() == null || roomType.getAmenities().trim().isEmpty()) {
            throw new RuntimeException("Phải có ít nhất 1 tiện nghi");
=======
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        
        // Check if the name is unique
        if (roomType.getId() == null) {
            RoomType existingRoomType = roomTypeRepository.findByName(roomType.getName());
            if (existingRoomType != null) {
                throw new BusinessException(ErrorCodes.REQUIRED_FIELD_UNIQUE, "There is already a room type with the same name");
            }
        } else {
            // For updates, check if the name is unique among other room types
            RoomType existingRoomType = roomTypeRepository.findByName(roomType.getName());
            if (existingRoomType != null && !existingRoomType.getId().equals(roomType.getId())) {
                throw new BusinessException(ErrorCodes.REQUIRED_FIELD_UNIQUE, "There is already a room type with the same name");
            }
>>>>>>> Stashed changes
        }
        
        return roomTypeRepository.save(roomType);
    }
    
    @Transactional
    public void deleteRoomType(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }
        
        // Check if the room type exists
        getRoomTypeById(id);
        
        // Check if the room type is in use
        boolean inUse = roomRepository.findAll().stream()
                .anyMatch(room -> room.getRoomType().getId().equals(id));
        
        if (inUse) {
            throw new BusinessException(ErrorCodes.ROOM_TYPE_HAS_ROOMS);
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
        if (roomTypeId == null) {
            throw new IllegalArgumentException("Room type ID cannot be null");
        }
        
        // Check if the room type exists
        getRoomTypeById(roomTypeId);
        
        return roomRepository.findAll().stream()
                .anyMatch(room -> room.getRoomType().getId().equals(roomTypeId));
    }
}