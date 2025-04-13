package com.HotelManagement.dto;

/**
 * Data Transfer Object for room selections in booking requests
 */
public class RoomSelectionDto {
    private Integer roomTypeId;
    private Integer count;
    private Integer roomId; // Added for direct room selection
    
    public RoomSelectionDto() {
    }

    public RoomSelectionDto(Integer roomTypeId, Integer count) {
        this.roomTypeId = roomTypeId;
        this.count = count;
    }

    public Integer getRoomTypeId() {
        return roomTypeId;
    }
    
    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    public Integer getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "RoomSelectionDto{" +
                "roomTypeId=" + roomTypeId +
                ", count=" + count +
                '}';
    }
}